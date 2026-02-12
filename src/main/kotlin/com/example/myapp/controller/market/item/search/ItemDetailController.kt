package com.example.myapp.controller.market.item.search

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.detail.ItemDetailResponse
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.item.ItemDetailService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 商品詳細取得コントローラー
 * 商品の詳細情報を取得します(閲覧用)。
 */
@RestController
@RequestMapping("/v1/market/items")
class ItemDetailController(
    private val itemDetailService: ItemDetailService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    /**
     * 商品詳細取得
     */
    @GetMapping("/{itemId}")
    fun getItemDetail(
        @PathVariable itemId: String,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ItemDetailResponse> {
        val (userId, guestId) = servletRequest.getMarketUser(appCookieService, sessionService)

        val result = itemDetailService.getItemDetail(itemId, userId, guestId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        return ResponseEntity.ok(result)
    }
}
