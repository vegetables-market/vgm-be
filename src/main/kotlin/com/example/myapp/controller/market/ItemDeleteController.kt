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
class ItemDeleteController(
    private val itemService: ItemService
) {

    private fun requireAuth(userSession: UserSession?): UserSession {
        return userSession ?: throw BusinessException(
            errorCode = "UNAUTHORIZED",
            message = "ログインが必要です",
            httpStatus = HttpStatus.UNAUTHORIZED
        )
    }

    @DeleteMapping("/{itemId}")
    fun deleteItem(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long
    ): ResponseEntity<Void> {
        val session = requireAuth(userSession)
        itemService.deleteItem(session.userId, itemId)
        return ResponseEntity.ok().build()
    }
}
