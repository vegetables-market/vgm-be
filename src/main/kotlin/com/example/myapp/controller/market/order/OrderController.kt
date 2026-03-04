package com.example.myapp.controller.market.order

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.checkout.CheckoutItemRequest
import com.example.myapp.dto.market.checkout.CheckoutRequest
import com.example.myapp.dto.market.checkout.CheckoutShippingRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.checkout.CheckoutService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/orders")
class OrderController(
    private val checkoutService: CheckoutService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService,
) {

    @PostMapping
    fun createOrder(
        @RequestBody rawRequest: Map<String, Any?>,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val request = CheckoutRequest(
            items = listOf(
                CheckoutItemRequest(
                    itemId = rawRequest.requiredString("itemId", "item_id", errorMessage = "itemId is required"),
                    quantity = rawRequest.requiredInt("quantity"),
                ),
            ),
            shipping = CheckoutShippingRequest(
                name = rawRequest.requiredString("shippingName", "shipping_name", errorMessage = "shippingName is required"),
                zipCode = rawRequest.requiredString("shippingZipCode", "shipping_zip_code", errorMessage = "shippingZipCode is required"),
                prefecture = rawRequest.requiredString("shippingPrefecture", "shipping_prefecture", errorMessage = "shippingPrefecture is required"),
                city = rawRequest.requiredString("shippingCity", "shipping_city", errorMessage = "shippingCity is required"),
                addressLine1 = rawRequest.requiredString("shippingAddressLine1", "shipping_address_line1", errorMessage = "shippingAddressLine1 is required"),
                addressLine2 = rawRequest.optionalString("shippingAddressLine2", "shipping_address_line2"),
            ),
            paymentMethod = rawRequest.optionalString("paymentMethod", "payment_method"),
            idempotencyKey = rawRequest.optionalString("idempotencyKey", "idempotency_key"),
        )

        val response = checkoutService.checkout(userId, request)
        return ResponseEntity.ok(
            mapOf(
                "orderId" to response.orderId,
                "totalAmount" to response.totalAmount,
                "status" to response.status.toLegacyOrderStatus(),
            ),
        )
    }

    @PostMapping("/{orderId}/pay")
    fun payOrder(
        @PathVariable orderId: Long,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, String>> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        checkoutService.pay(userId, orderId, null)
        return ResponseEntity.ok(mapOf("status" to "success"))
    }
}

private fun Map<String, Any?>.requiredString(
    vararg keys: String,
    errorMessage: String,
): String {
    return optionalString(*keys)
        ?: throw AppException(ErrorCode.INVALID_INPUT, errorMessage)
}

private fun Map<String, Any?>.optionalString(vararg keys: String): String? {
    val raw = keys.firstNotNullOfOrNull { this[it] } ?: return null
    return raw.toString().trim().takeIf { it.isNotEmpty() }
}

private fun Map<String, Any?>.requiredInt(key: String): Int {
    val raw = this[key]
    val value = when (raw) {
        is Number -> raw.toInt()
        is String -> raw.toIntOrNull()
        else -> null
    }
    return value ?: throw AppException(ErrorCode.INVALID_INPUT, "$key must be a number")
}

private fun String.toLegacyOrderStatus(): Int {
    return when (uppercase()) {
        "PENDING_PAYMENT" -> 1
        "PAID" -> 2
        else -> 0
    }
}
