package com.example.myapp.controller.market.favorite

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.GuestSessionService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.market.FavoriteService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/favorites")
class AddFavoriteController(
    private val favoriteService: FavoriteService,
    private val sessionService: SessionService,
    private val guestSessionService: GuestSessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/{itemId}")
    fun addFavorite(
        @PathVariable itemId: Long,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val (userId, currentGuestId) = servletRequest.getMarketUser(appCookieService, sessionService)
        
        val effectiveGuestId = if (userId == null) {
             guestSessionService.ensureGuestSession(currentGuestId, servletResponse)
        } else null

        favoriteService.addFavorite(userId, effectiveGuestId, itemId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "お気に入りに追加しました"
        ))
    }
}
