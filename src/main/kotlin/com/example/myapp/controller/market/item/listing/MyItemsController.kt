package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.SimpleItemResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.item.ItemService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 出品済み商品一覧取得コントローラー
 * ログインユーザー自身が出品した商品の一覧を取得します。
 */
@RestController
@RequestMapping("/v1/market/items")
class MyItemsController(
    private val itemService: ItemService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @GetMapping("/me")
    fun getMyItems(servletRequest: HttpServletRequest): ResponseEntity<List<SimpleItemResponse>> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val response = itemService.getMyItems(userId)
        return ResponseEntity.ok(response)
    }
}
