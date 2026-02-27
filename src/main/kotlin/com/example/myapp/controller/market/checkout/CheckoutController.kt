package com.example.myapp.controller.market.checkout

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.checkout.CheckoutItemRequest
import com.example.myapp.dto.market.checkout.CheckoutPayRequest
import com.example.myapp.dto.market.checkout.CheckoutPayResponse
import com.example.myapp.dto.market.checkout.CheckoutRequest
import com.example.myapp.dto.market.checkout.CheckoutResponse
import com.example.myapp.dto.market.checkout.CheckoutShippingRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.checkout.CheckoutService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/checkout")
class CheckoutController(
    private val checkoutService: CheckoutService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService,
) {
    @PostMapping
    fun checkout(
        @RequestBody rawRequest: Map<String, Any?>,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<CheckoutResponse> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val itemsRaw = rawRequest["items"] as? List<*>
            ?: throw AppException(ErrorCode.INVALID_INPUT, "items is required")

        val items = itemsRaw.map { item ->
            val map = item as? Map<*, *>
                ?: throw AppException(ErrorCode.INVALID_INPUT, "Invalid item format")
            val itemId = (map["itemId"] ?: map["item_id"])
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw AppException(ErrorCode.INVALID_INPUT, "itemId is required")
            val quantity = when (val raw = map["quantity"]) {
                is Number -> raw.toInt()
                is String -> raw.toIntOrNull()
                else -> null
            } ?: throw AppException(ErrorCode.INVALID_INPUT, "quantity must be a number")

            CheckoutItemRequest(itemId = itemId, quantity = quantity)
        }

        val shippingRaw = rawRequest["shipping"] as? Map<*, *>
            ?: throw AppException(ErrorCode.INVALID_INPUT, "shipping is required")

        val shipping = CheckoutShippingRequest(
            name = shippingRaw["name"]?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw AppException(ErrorCode.INVALID_INPUT, "shipping.name is required"),
            zipCode = (shippingRaw["zipCode"] ?: shippingRaw["zip_code"])
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw AppException(ErrorCode.INVALID_INPUT, "shipping.zipCode is required"),
            prefecture = shippingRaw["prefecture"]?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw AppException(ErrorCode.INVALID_INPUT, "shipping.prefecture is required"),
            city = shippingRaw["city"]?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw AppException(ErrorCode.INVALID_INPUT, "shipping.city is required"),
            addressLine1 = (shippingRaw["addressLine1"] ?: shippingRaw["address_line1"])
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw AppException(ErrorCode.INVALID_INPUT, "shipping.addressLine1 is required"),
            addressLine2 = (shippingRaw["addressLine2"] ?: shippingRaw["address_line2"])?.toString(),
        )

        val request = CheckoutRequest(
            items = items,
            shipping = shipping,
            paymentMethod = (rawRequest["paymentMethod"] ?: rawRequest["payment_method"])?.toString(),
            idempotencyKey = (rawRequest["idempotencyKey"] ?: rawRequest["idempotency_key"])?.toString(),
        )

        return ResponseEntity.ok(checkoutService.checkout(userId, request))
    }

    @PostMapping("/{orderId}/pay")
    fun pay(
        @PathVariable orderId: Long,
        @RequestBody(required = false) request: CheckoutPayRequest?,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<CheckoutPayResponse> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }
        return ResponseEntity.ok(checkoutService.pay(userId, orderId))
    }
}
