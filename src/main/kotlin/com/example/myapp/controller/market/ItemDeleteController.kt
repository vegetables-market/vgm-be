package com.example.myapp.controller.market

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.market.ItemService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/items")
class ItemDeleteController(
    private val itemService: ItemService
) {

    @DeleteMapping("/{itemId}")
    fun deleteItem(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long
    ): ResponseEntity<Void> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        itemService.deleteItem(userSession.userId, itemId)
        return ResponseEntity.ok().build()
    }
}
