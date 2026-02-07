package com.example.myapp.controller.market.cart

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.UpdateCartRequest
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.market.CartService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/cart")
class UpdateCartItemController(
    private val cartService: CartService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PutMapping("/{cartItemId}")
    fun updateCartItem(
        @PathVariable cartItemId: Long,
        @RequestBody request: UpdateCartRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        val (userId, guestId) = servletRequest.getMarketUser(appCookieService, sessionService)
        
        cartService.updateQuantity(cartItemId, request.quantity, userId, guestId)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
