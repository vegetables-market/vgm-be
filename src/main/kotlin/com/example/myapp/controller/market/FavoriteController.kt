package com.example.myapp.controller.market

import com.example.myapp.dto.market.ItemResponse
import com.example.myapp.dto.market.PaginatedResponse
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.market.FavoriteService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/favorites")
class FavoriteController(
    private val favoriteService: FavoriteService,
    private val userSessionRepository: UserSessionRepository,
    private val guestSessionService: com.example.myapp.service.auth.GuestSessionService
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

    /**
     * お気に入りに追加
     */
    @PostMapping("/{itemId}")
    fun addFavorite(
        @PathVariable itemId: Long,
        servletRequest: HttpServletRequest,
        servletResponse: jakarta.servlet.http.HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
        
        val guestId = if (userId == null) {
             val currentGuestId = servletRequest.cookies?.find { it.name == com.example.myapp.service.auth.GuestSessionService.GUEST_COOKIE_NAME }?.value
             guestSessionService.ensureGuestSession(currentGuestId, servletResponse)
        } else null

        return try {
            favoriteService.addFavorite(userId, guestId, itemId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "お気に入りに追加しました"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(mapOf("error" to (e.message ?: "エラーが発生しました")))
        }
    }

    /**
     * お気に入りから削除
     */
    @DeleteMapping("/{itemId}")
    fun removeFavorite(
        @PathVariable itemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
        val guestId = if (userId == null) {
            servletRequest.cookies?.find { it.name == com.example.myapp.service.auth.GuestSessionService.GUEST_COOKIE_NAME }?.value
        } else null

        if (userId == null && guestId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))
        }

        favoriteService.removeFavorite(userId, guestId, itemId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "お気に入りから削除しました"
        ))
    }

    /**
     * お気に入り一覧取得
     */
    @GetMapping
    fun getFavorites(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        servletRequest: HttpServletRequest
    ): ResponseEntity<PaginatedResponse<ItemResponse>> {
        val userId = getUserIdFromSession(servletRequest)
        val guestId = if (userId == null) {
            servletRequest.cookies?.find { it.name == com.example.myapp.service.auth.GuestSessionService.GUEST_COOKIE_NAME }?.value
        } else null

        if (userId == null && guestId == null) {
            // ゲストIDもない場合は空リストを返すべきか認証エラーか。フロントエンドの挙動によるが、一旦空リストでなく401を維持（既存踏襲）
            // ただしゲスト利用を促進するなら空リストの方が優しいかもしれない。
            // ここでは既存の「ログインが必要です」の代わりに、認証コンテキストがない場合は401を返す。
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val result = favoriteService.getFavorites(userId, guestId, page, limit)
        return ResponseEntity.ok(result)
    }
}
