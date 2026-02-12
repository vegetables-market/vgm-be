package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.item.ItemService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 商品ステータス更新コントローラー
 * 公開停止、再公開など、商品の状態を変更します。
 */
@RestController
@RequestMapping("/v1/market/items")
class UpdateStatusController(
    private val itemService: ItemService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    data class UpdateStatusRequest(
        val status: Int
    )

    @PatchMapping("/{itemId}/status")
    fun updateItemStatus(
        @PathVariable itemId: String,
        @RequestBody request: UpdateStatusRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        itemService.updateItemStatus(userId, itemId, request.status)
        return ResponseEntity.ok().build()
    }
}
