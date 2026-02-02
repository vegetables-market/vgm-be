package com.example.myapp.controller.market

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.market.ItemService
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

    @PatchMapping("/{itemId}/status")
    fun updateItemStatus(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<Void> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        itemService.updateItemStatus(userSession.userId, itemId, request.status)
        return ResponseEntity.ok().build()
    }
}
