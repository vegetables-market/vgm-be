package com.example.myapp.controller.market.favorite

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.ItemResponse
import com.example.myapp.dto.market.PaginatedResponse
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.FavoriteService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/favorites")
class GetFavoritesController(
    private val favoriteService: FavoriteService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @GetMapping
    fun getFavorites(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        servletRequest: HttpServletRequest
    ): ResponseEntity<PaginatedResponse<ItemResponse>> {
        val (userId, guestId) = servletRequest.getMarketUser(appCookieService, sessionService)

        if (userId == null && guestId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val result = favoriteService.getFavorites(userId, guestId, page, limit)
        return ResponseEntity.ok(result)
    }
}
