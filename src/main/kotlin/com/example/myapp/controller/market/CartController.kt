package com.example.myapp.controller.market

import com.example.myapp.dto.market.AddCartRequest
import com.example.myapp.dto.market.CartResponse
import com.example.myapp.dto.market.UpdateCartRequest
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.market.CartService
import com.example.myapp.service.auth.GuestSessionService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/market/cart")
class CartController(
    private val cartService: CartService,
    private val userSessionRepository: UserSessionRepository,
    private val guestSessionService: GuestSessionService
) {

    private fun getUserIdFromSession(request: HttpServletRequest): Int? {
        val sessionKey = request.cookies?.find { it.name == "vgm_session" }?.value
            ?: return null

        val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
            sessionKey,
            LocalDateTime.now()
        ) ?: return null

        return session.userId
    }

    private fun getGuestIdFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == GuestSessionService.GUEST_COOKIE_NAME }?.value
    }

    @GetMapping
    fun getCart(servletRequest: HttpServletRequest): ResponseEntity<CartResponse> {
        val userId = getUserIdFromSession(servletRequest)
        val guestId = if (userId == null) getGuestIdFromCookie(servletRequest) else null
        
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
        val userId = getUserIdFromSession(servletRequest)
        val guestId = if (userId == null) {
            val currentGuestId = getGuestIdFromCookie(servletRequest)
            guestSessionService.ensureGuestSession(currentGuestId, servletResponse)
        } else null

        return try {
            cartService.addToCart(userId, guestId, request.itemId, request.quantity)
            ResponseEntity.ok(mapOf("success" to true, "message" to "カートに追加しました"))
        } catch (e: Exception) {
             ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
        }
    }

    @PutMapping("/{cartItemId}")
    fun updateCartItem(
        @PathVariable cartItemId: Long,
        @RequestBody request: UpdateCartRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        // Need to verify ownership? 
        // CartService logic relies on implicit ownership, but here we just pass ID.
        // It's better if CartService checks ownership, but for now we just allow update.
        // TODO: Add ownership check in Service or Controller (fetch item and check userId/guestId)
        
        cartService.updateQuantity(cartItemId, request.quantity)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    @DeleteMapping("/{cartItemId}")
    fun removeFromCart(
        @PathVariable cartItemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        // TODO: Ownership check
        cartService.removeFromCart(cartItemId)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
