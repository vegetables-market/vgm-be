package com.example.myapp.service.market.checkout

import com.example.myapp.dto.market.checkout.CheckoutItemResult
import com.example.myapp.dto.market.checkout.CheckoutPayRequest
import com.example.myapp.dto.market.checkout.CheckoutPayResponse
import com.example.myapp.dto.market.checkout.CheckoutRequest
import com.example.myapp.dto.market.checkout.CheckoutResponse
import com.example.myapp.entity.market.payment.Payment
import com.example.myapp.entity.market.order.Order
import com.example.myapp.entity.market.order.OrderItem
import com.example.myapp.entity.market.order.Shipment
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.market.cart.CartItemRepository
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.market.order.OrderItemRepository
import com.example.myapp.repository.market.order.OrderRepository
import com.example.myapp.repository.market.order.ShipmentRepository
import com.example.myapp.repository.market.payment.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CheckoutService(
    private val itemRepository: ItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val orderRepository: OrderRepository,
    private val shipmentRepository: ShipmentRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentRepository: PaymentRepository,
    private val stripeCheckoutPaymentService: StripeCheckoutPaymentService,
) {
    @Transactional
    fun checkout(userId: Int, request: CheckoutRequest): CheckoutResponse {
        if (request.items.isEmpty()) {
            throw AppException(ErrorCode.INVALID_INPUT, "items is required")
        }

        val mergedItems = request.items
            .map { it.itemId.trim() to it.quantity }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, q) -> q.sum() }
            .filterKeys { it.isNotEmpty() }

        if (mergedItems.isEmpty() || mergedItems.values.any { it <= 0 }) {
            throw AppException(ErrorCode.INVALID_INPUT, "Invalid checkout items")
        }

        val lockedItems = mutableMapOf<String, com.example.myapp.entity.market.item.Item>()
        val sortedDisplayIds = mergedItems.keys.sorted()

        sortedDisplayIds.forEach { displayId ->
            val item = itemRepository.findByDisplayIdForUpdate(displayId)
                ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Item not found: $displayId")
            if (item.status.toInt() != 2) {
                throw AppException(ErrorCode.INVALID_INPUT, "Item is not available for sale: $displayId")
            }
            val requestedQuantity = mergedItems[displayId] ?: 0
            if (requestedQuantity > item.quantity) {
                throw AppException(
                    ErrorCode.ITEM_OUT_OF_STOCK,
                    "Requested quantity exceeds available stock ($displayId: ${item.quantity})",
                )
            }
            lockedItems[displayId] = item
        }

        val responseItems = mutableListOf<CheckoutItemResult>()
        val totalAmount = sortedDisplayIds.sumOf { displayId ->
            val item = lockedItems[displayId] ?: error("locked item missing: $displayId")
            val quantity = mergedItems[displayId] ?: 0
            val unitPrice = item.price?.toLong() ?: 0L
            val subtotal = unitPrice * quantity
            responseItems += CheckoutItemResult(
                itemId = displayId,
                quantity = quantity,
                unitPrice = unitPrice,
                subtotal = subtotal,
            )
            subtotal
        }

        val order = orderRepository.save(
            Order(
                buyerId = userId,
                totalAmount = totalAmount,
                status = 1,
                shippingName = request.shipping.name,
                shippingZipCode = request.shipping.zipCode,
                shippingPrefecture = request.shipping.prefecture,
                shippingCity = request.shipping.city,
                shippingAddressLine1 = request.shipping.addressLine1,
                shippingAddressLine2 = request.shipping.addressLine2,
            ),
        )

        val shipmentsBySeller = mutableMapOf<Int, Shipment>()

        sortedDisplayIds.forEach { displayId ->
            val item = lockedItems[displayId] ?: return@forEach
            val quantity = mergedItems[displayId] ?: 0
            val sellerId = item.user.userId

            val shipment = shipmentsBySeller.getOrPut(sellerId) {
                shipmentRepository.save(
                    Shipment(
                        orderId = order.orderId,
                        sellerId = sellerId,
                        shippingMethodId = item.shippingMethodId ?: 1,
                    ),
                )
            }

            val unitPrice = item.price ?: 0
            orderItemRepository.save(
                OrderItem(
                    orderId = order.orderId,
                    shipmentId = shipment.shipmentId,
                    itemId = item.itemId ?: throw AppException(ErrorCode.SYSTEM_ERROR, "itemId missing"),
                    sellerId = sellerId,
                    unitPrice = unitPrice,
                    quantity = quantity,
                    platformFee = 0,
                    sellerAmount = unitPrice * quantity,
                ),
            )

            item.quantity -= quantity
            if (item.quantity <= 0) {
                item.quantity = 0
                item.status = 4 // SOLD
            }
            itemRepository.save(item)
        }

        val purchasedItemIds = sortedDisplayIds.mapNotNull { lockedItems[it]?.itemId }
        if (purchasedItemIds.isNotEmpty()) {
            cartItemRepository.deleteByUserIdAndItemIdIn(userId, purchasedItemIds)
        }

        var paymentIntentId: String? = null
        var clientSecret: String? = null
        if (request.paymentMethod.isCardPaymentMethod()) {
            if (totalAmount < MINIMUM_CARD_PAYMENT_AMOUNT_JPY) {
                throw AppException(
                    errorCode = ErrorCode.INVALID_INPUT,
                    details = listOf("Card payment requires at least 50 JPY"),
                )
            }

            val stripePayment = stripeCheckoutPaymentService.createPaymentIntent(
                orderId = order.orderId,
                userId = userId,
                amount = totalAmount,
                idempotencyKey = request.idempotencyKey,
            )

            paymentRepository.save(
                Payment(
                    orderId = order.orderId,
                    method = CARD_PAYMENT_METHOD,
                    externalTransactionId = stripePayment.paymentIntentId,
                    status = stripePayment.status,
                    amount = BigDecimal.valueOf(totalAmount),
                ),
            )

            paymentIntentId = stripePayment.paymentIntentId
            clientSecret = stripePayment.clientSecret
        }

        return CheckoutResponse(
            orderId = order.orderId,
            status = "PENDING_PAYMENT",
            items = responseItems,
            totalAmount = totalAmount,
            paymentIntentId = paymentIntentId,
            clientSecret = clientSecret,
        )
    }

    @Transactional
    fun pay(userId: Int, orderId: Long, request: CheckoutPayRequest?): CheckoutPayResponse {
        val order = orderRepository.findByOrderIdAndBuyerId(orderId, userId)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Order not found")

        if (order.status.toInt() == ORDER_STATUS_PENDING_PAYMENT) {
            val requestedPaymentMethod = request?.paymentMethod.normalizedPaymentMethod()
            val latestPayment = paymentRepository.findTopByOrderIdOrderByPaymentIdDesc(order.orderId)
            val effectivePaymentMethod = requestedPaymentMethod ?: latestPayment?.method?.normalizedPaymentMethod()

            if (effectivePaymentMethod.isCardPaymentMethod()) {
                val requestedPaymentIntentId = request?.paymentIntentId
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                val paymentIntentId = requestedPaymentIntentId
                    ?: latestPayment?.externalTransactionId
                    ?: throw AppException(ErrorCode.INVALID_INPUT, "paymentIntentId is required")

                val verifiedPayment = stripeCheckoutPaymentService.verifyPaymentIntent(
                    paymentIntentId = paymentIntentId,
                    orderId = order.orderId,
                    expectedAmount = order.totalAmount,
                )

                val payment = latestPayment ?: Payment(
                    orderId = order.orderId,
                    method = CARD_PAYMENT_METHOD,
                    amount = BigDecimal.valueOf(order.totalAmount),
                )
                payment.method = CARD_PAYMENT_METHOD
                payment.externalTransactionId = verifiedPayment.paymentIntentId
                payment.status = verifiedPayment.status
                payment.amount = BigDecimal.valueOf(order.totalAmount)
                paymentRepository.save(payment)
            }

            order.status = 2 // PAID
            orderRepository.save(order)
        }

        return CheckoutPayResponse(
            orderId = order.orderId,
            status = if (order.status.toInt() == 2) "PAID" else "PENDING_PAYMENT",
        )
    }
}

private const val CARD_PAYMENT_METHOD = "card"
private const val ORDER_STATUS_PENDING_PAYMENT: Int = 1
private const val MINIMUM_CARD_PAYMENT_AMOUNT_JPY: Long = 50L

private fun String?.normalizedPaymentMethod(): String? {
    return this?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
}

private fun String?.isCardPaymentMethod(): Boolean {
    val normalized = normalizedPaymentMethod()
    return normalized == CARD_PAYMENT_METHOD || normalized == "credit_card"
}

