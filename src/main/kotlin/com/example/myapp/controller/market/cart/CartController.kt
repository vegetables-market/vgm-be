package com.example.myapp.controller.market.cart

import com.example.myapp.dto.market.AddCartRequest
import com.example.myapp.dto.market.CartResponse
import com.example.myapp.dto.market.UpdateCartRequest
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.GuestSessionService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.market.CartService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/cart")
class CartController(
    private val cartService: CartService,
    private val sessionService: SessionService,
    private val guestSessionService: GuestSessionService,
    private val appCookieService: AppCookieService
) {

    private fun getCurrentUser(request: HttpServletRequest): Pair<Int?, String?> {
        val sessionKey = appCookieService.getSessionCookie(request)
        val userId = sessionKey?.let { sessionService.getValidSession(it)?.userId }
        val guestId = if (userId == null) appCookieService.getGuestCookie(request) else null
        return userId to guestId
    }

    @GetMapping
    fun getCart(servletRequest: HttpServletRequest): ResponseEntity<CartResponse> {
        val (userId, guestId) = getCurrentUser(servletRequest)

        if (userId == null && guestId == null) {
             return ResponseEntity.ok(CartResponse(items = emptyList(), totalAmount = 0L))
        }

        val cart = cartService.getCart(userId, guestId)
        return ResponseEntity.ok(cart)
    }

    @PostMapping
    fun addToCart(
        @RequestBody request: AddCartRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val (userId, currentGuestId) = getCurrentUser(servletRequest)

        // Ensure guest session if not logged in
        val effectiveGuestId = if (userId == null) {
             guestSessionService.ensureGuestSession(currentGuestId, servletResponse)
        } else null

        cartService.addToCart(userId, effectiveGuestId, request.itemId, request.quantity)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "カートに追加しました"))
    }

    @PutMapping("/{cartItemId}")
    fun updateCartItem(
        @PathVariable cartItemId: Long,
        @RequestBody request: UpdateCartRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        val (userId, guestId) = getCurrentUser(servletRequest)

        cartService.updateQuantity(cartItemId, request.quantity, userId, guestId)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    @DeleteMapping("/{cartItemId}")
    fun removeFromCart(
        @PathVariable cartItemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        val (userId, guestId) = getCurrentUser(servletRequest)

        cartService.removeFromCart(cartItemId, userId, guestId)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}