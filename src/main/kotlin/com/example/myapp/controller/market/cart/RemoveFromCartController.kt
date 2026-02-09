package com.example.myapp.controller.market.cart

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.CartService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/cart")
class RemoveFromCartController(
    private val cartService: CartService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @DeleteMapping("/{cartItemId}")
    fun removeFromCart(
        @PathVariable cartItemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        val (userId, guestId) = servletRequest.getMarketUser(appCookieService, sessionService)
        
        cartService.removeFromCart(cartItemId, userId, guestId)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
