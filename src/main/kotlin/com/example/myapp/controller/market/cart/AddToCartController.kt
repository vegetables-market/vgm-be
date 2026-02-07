package com.example.myapp.controller.market.cart

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.AddCartRequest
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.GuestSessionService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.market.CartService
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
    private val cartService: CartService,
    private val sessionService: SessionService,
    private val guestSessionService: GuestSessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping
    fun addToCart(
        @RequestBody request: AddCartRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val (userId, currentGuestId) = servletRequest.getMarketUser(appCookieService, sessionService)
        
        // Ensure guest session if not logged in
        val effectiveGuestId = if (userId == null) {
             guestSessionService.ensureGuestSession(currentGuestId, servletResponse)
        } else null

        cartService.addToCart(userId, effectiveGuestId, request.itemId, request.quantity)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "カートに追加しました"))
    }
}
