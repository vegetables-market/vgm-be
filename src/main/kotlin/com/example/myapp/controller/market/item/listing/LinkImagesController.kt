package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.LinkImagesRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.market.ItemService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 商品画像紐付けコントローラー
 * アップロードされた画像を商品データ(DB)と紐付けます。
 */
@RestController
@RequestMapping("/v1/market/items")
class LinkImagesController(
    private val itemService: ItemService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/{itemId}/images")
    fun linkImages(
        @PathVariable itemId: Long,
        @RequestBody request: LinkImagesRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }
        itemService.linkImages(userId, itemId, request.filenames)
        return ResponseEntity.ok().build()
    }
}
