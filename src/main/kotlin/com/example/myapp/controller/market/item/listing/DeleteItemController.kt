package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.item.ItemService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 商品削除コントローラー
 * 出品した商品を削除（論理削除または物理削除）します。
 */
@RestController
@RequestMapping("/v1/market/items")
class DeleteItemController(
    private val itemService: ItemService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @DeleteMapping("/{itemId}")
    fun deleteItem(
        @PathVariable itemId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        itemService.deleteItem(userId, itemId)
        return ResponseEntity.ok().build()
    }
}
