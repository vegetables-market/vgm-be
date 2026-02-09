package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.liisting.CreateDraftResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.item.ItemService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 商品下書き作成コントローラー
 * 出品プロセスの最初に呼び出され、下書き状態の商品を作成します。
 */
@RestController
@RequestMapping("/v1/market/items")
class CreateDraftController(
    private val itemService: ItemService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/draft")
    fun createDraft(servletRequest: HttpServletRequest): ResponseEntity<CreateDraftResponse> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }
        val item = itemService.createDraft(userId)
        return ResponseEntity.ok(CreateDraftResponse(itemId = item.itemId!!))
    }
}
