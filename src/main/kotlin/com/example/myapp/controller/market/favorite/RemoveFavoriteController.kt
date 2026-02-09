package com.example.myapp.controller.market.favorite

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.favorite.RemoveFavorite
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/favorites")
class RemoveFavoriteController(
    private val removeFavorite: RemoveFavorite,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @DeleteMapping("/{itemId}")
    fun removeFavorite(
        @PathVariable itemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, guestId) = servletRequest.getMarketUser(appCookieService, sessionService)

        if (userId == null && guestId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        removeFavorite(userId, guestId, itemId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "お気に入りから削除しました"
        ))
    }
}
