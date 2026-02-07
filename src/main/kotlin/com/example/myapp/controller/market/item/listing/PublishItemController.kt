package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.CreateItemRequest
import com.example.myapp.dto.market.item.SimpleItemResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.market.ItemService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 商品公開（出品完了）コントローラー
 * 下書き状態の商品情報を更新し、公開状態にします。
 */
@RestController
@RequestMapping("/v1/market/items")
class PublishItemController(
    private val itemService: ItemService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PutMapping("/{itemId}")
    fun publishItem(
        @PathVariable itemId: Long,
        @RequestBody request: CreateItemRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<SimpleItemResponse> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }
        val response = itemService.publishItem(userId, itemId, request)
        return ResponseEntity.ok(response)
    }
}
