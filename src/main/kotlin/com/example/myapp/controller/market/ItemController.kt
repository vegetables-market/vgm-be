package com.example.myapp.controller.market

import com.example.myapp.dto.market.CreateItemRequest
import com.example.myapp.dto.market.ItemResponse
import com.example.myapp.dto.market.SimpleItemResponse
import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.market.ItemService
import com.example.myapp.service.market.MediaService
import com.example.myapp.service.market.UploadTokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/items")
class ItemController(
    private val itemService: ItemService,
    private val mediaService: MediaService
) {

    @PostMapping("/upload-token")
    fun getUploadToken(@AuthenticationPrincipal userSession: UserSession?): UploadTokenResponse {
        if (userSession == null) throw RuntimeException("Unauthorized")
        return mediaService.generateUploadToken(userSession.userId, "USER")
    }

    @PostMapping("/draft")
    fun createDraft(@AuthenticationPrincipal userSession: UserSession?): ResponseEntity<Map<String, Long>> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        val item = itemService.createDraft(userSession.userId)
        return ResponseEntity.ok(mapOf("item_id" to item.itemId!!))
    }

    @PostMapping("/{itemId}/images")
    fun linkImages(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long,
        @RequestBody request: com.example.myapp.dto.market.LinkImagesRequest
    ): ResponseEntity<Void> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        itemService.linkImages(userSession.userId, itemId, request.filenames)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{itemId}")
    fun publishItem(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long,
        @RequestBody request: CreateItemRequest
    ): ResponseEntity<SimpleItemResponse> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        val response = itemService.publishItem(userSession.userId, itemId, request)
        return ResponseEntity.ok(response)
    }

    // 既存のcreateItem (互換性のため残すか、削除してドラフトフローに移行)
    // 今回は新規フローへの移行を推奨するため、一旦残すが使用しない想定
    @PostMapping
    fun createItem(
        @AuthenticationPrincipal userSession: UserSession?,
        @RequestBody request: CreateItemRequest
    ): ResponseEntity<SimpleItemResponse> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        val response = itemService.createItem(userSession.userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMyItems(
        @AuthenticationPrincipal userSession: UserSession?
    ): ResponseEntity<List<SimpleItemResponse>> {
        if (userSession == null) throw RuntimeException("Unauthorized")
        val response = itemService.getMyItems(userSession.userId)
        return ResponseEntity.ok(response)
    }
}
