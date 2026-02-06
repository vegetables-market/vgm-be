package com.example.myapp.controller.market

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.exception.BusinessException
import com.example.myapp.service.market.ItemService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/items")
class ItemStatusController(
    private val itemService: ItemService
) {

    data class UpdateStatusRequest(
        val status: Int
    )

    private fun requireAuth(userSession: UserSession?): UserSession {
        return userSession ?: throw BusinessException(
            errorCode = "UNAUTHORIZED",
            message = "ログインが必要です",
            httpStatus = HttpStatus.UNAUTHORIZED
        )
    }

    @PatchMapping("/{itemId}/status")
    fun updateItemStatus(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Void> {
        val session = requireAuth(userSession)
        itemService.updateItemStatus(session.userId, itemId, request.status)
        return ResponseEntity.ok().build()
    }
}
