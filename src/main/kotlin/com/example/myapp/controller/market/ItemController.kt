package com.example.myapp.controller.market

import com.example.myapp.dto.market.CreateItemRequest
import com.example.myapp.dto.market.ItemResponse
import com.example.myapp.dto.market.SimpleItemResponse
import com.example.myapp.entity.auth.UserSession
import com.example.myapp.exception.BusinessException
import com.example.myapp.service.market.ItemService
import com.example.myapp.service.market.MediaService
import com.example.myapp.service.market.UploadTokenResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/items")
class ItemController(
    private val itemService: ItemService,
    private val mediaService: MediaService
) {

    private fun requireAuth(userSession: UserSession?): UserSession {
        return userSession ?: throw BusinessException(
            errorCode = "UNAUTHORIZED",
            message = "ログインが必要です",
            httpStatus = HttpStatus.UNAUTHORIZED
        )
    }

    @PostMapping("/upload-token")
    fun getUploadToken(@AuthenticationPrincipal userSession: UserSession?): UploadTokenResponse {
        val session = requireAuth(userSession)
        return mediaService.generateUploadToken(session.userId, "USER")
    }

    @PostMapping("/draft")
    fun createDraft(@AuthenticationPrincipal userSession: UserSession?): ResponseEntity<Map<String, Long>> {
        val session = requireAuth(userSession)
        val item = itemService.createDraft(session.userId)
        return ResponseEntity.ok(mapOf("item_id" to item.itemId!!))
    }

    @PostMapping("/{itemId}/images")
    fun linkImages(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long,
        @RequestBody request: com.example.myapp.dto.market.LinkImagesRequest
    ): ResponseEntity<Void> {
        val session = requireAuth(userSession)
        itemService.linkImages(session.userId, itemId, request.filenames)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{itemId}")
    fun publishItem(
        @AuthenticationPrincipal userSession: UserSession?,
        @PathVariable itemId: Long,
        @RequestBody request: CreateItemRequest
    ): ResponseEntity<SimpleItemResponse> {
        val session = requireAuth(userSession)
        val response = itemService.publishItem(session.userId, itemId, request)
        return ResponseEntity.ok(response)
    }

    // 既存のcreateItem (互換性のため残すか、削除してドラフトフローに移行)
    // 今回は新規フローへの移行を推奨するため、一旦残すが使用しない想定
    @PostMapping
    fun createItem(
        @AuthenticationPrincipal userSession: UserSession?,
        @RequestBody request: CreateItemRequest
    ): ResponseEntity<SimpleItemResponse> {
        val session = requireAuth(userSession)
        val response = itemService.createItem(session.userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMyItems(
        @AuthenticationPrincipal userSession: UserSession?
    ): ResponseEntity<List<SimpleItemResponse>> {
        val session = requireAuth(userSession)
        val response = itemService.getMyItems(session.userId)
        return ResponseEntity.ok(response)
    }
}
