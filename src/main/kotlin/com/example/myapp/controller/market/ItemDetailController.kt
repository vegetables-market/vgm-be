package com.example.myapp.controller.market

import com.example.myapp.dto.market.ItemDetailResponse
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.market.ItemDetailService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/market/items")
class ItemDetailController(
    private val itemDetailService: ItemDetailService,
    private val userSessionRepository: UserSessionRepository
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
     * 商品詳細取得
     */
    /**
     * 商品詳細取得
     */
    @GetMapping("/{itemId}")
    fun getItemDetail(
        @PathVariable itemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ItemDetailResponse> {
        val userId = getUserIdFromSession(servletRequest)
        val guestId = if (userId == null) {
            servletRequest.cookies?.find { it.name == com.example.myapp.service.auth.GuestSessionService.GUEST_COOKIE_NAME }?.value
        } else null

        val result = itemDetailService.getItemDetail(itemId, userId, guestId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        return ResponseEntity.ok(result)
    }
}
