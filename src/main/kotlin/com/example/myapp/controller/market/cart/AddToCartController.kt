package com.example.myapp.controller.market.cart

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.GuestSessionService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.cart.AddToCart
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/cart")
class AddToCartController(
    private val addToCart: AddToCart,
    private val sessionService: SessionService,
    private val guestSessionService: GuestSessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping
    fun addToCart(
        @RequestBody request: Map<String, Any?>,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val (userId, currentGuestId) = servletRequest.getMarketUser(appCookieService, sessionService)

        val itemId = (request["itemId"] ?: request["item_id"])
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw AppException(ErrorCode.INVALID_INPUT, "itemId is required")

        val quantity = when (val raw = request["quantity"]) {
            null -> 1
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull()
            else -> null
        } ?: throw AppException(ErrorCode.INVALID_INPUT, "quantity must be a number")

        // Ensure guest session if not logged in
        val effectiveGuestId = if (userId == null) {
             guestSessionService.ensureGuestSession(currentGuestId, servletResponse)
        } else null

        addToCart(userId, effectiveGuestId, itemId, quantity)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "カートに追加しました"))
    }
}
