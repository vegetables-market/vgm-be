package com.example.myapp.service.market.checkout

import com.example.myapp.dto.market.checkout.CheckoutItemResult
import com.example.myapp.dto.market.checkout.CheckoutPayResponse
import com.example.myapp.dto.market.checkout.CheckoutRequest
import com.example.myapp.dto.market.checkout.CheckoutResponse
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CheckoutService(
    private val itemRepository: ItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val orderRepository: OrderRepository,
    private val shipmentRepository: ShipmentRepository,
    private val orderItemRepository: OrderItemRepository,
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

        return CheckoutResponse(
            orderId = order.orderId,
            status = "PENDING_PAYMENT",
            items = responseItems,
            totalAmount = totalAmount,
        )
    }

    @Transactional
    fun pay(userId: Int, orderId: Long): CheckoutPayResponse {
        val order = orderRepository.findByOrderIdAndBuyerId(orderId, userId)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Order not found")

        if (order.status.toInt() == 1) {
            order.status = 2 // PAID
            orderRepository.save(order)
        }

        return CheckoutPayResponse(
            orderId = order.orderId,
            status = if (order.status.toInt() == 2) "PAID" else "PENDING_PAYMENT",
        )
    }
}

