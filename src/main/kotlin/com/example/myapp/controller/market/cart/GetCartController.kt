package com.example.myapp.controller.market.cart

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.cart.CartResponse
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.CartService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/cart")
class GetCartController(
    private val cartService: CartService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @GetMapping
    fun getCart(servletRequest: HttpServletRequest): ResponseEntity<CartResponse> {
        val (userId, guestId) = servletRequest.getMarketUser(appCookieService, sessionService)
        
        if (userId == null && guestId == null) {
             return ResponseEntity.ok(CartResponse(items = emptyList(), totalAmount = 0L))
        }

        val cart = cartService.getCart(userId, guestId)
        return ResponseEntity.ok(cart)
    }
}
