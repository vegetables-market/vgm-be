package com.example.myapp.controller.market.item.listing

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.media.GenerateUploadToken
import com.example.myapp.service.market.media.UploadTokenResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 画像アップロード用トークン発行コントローラー
 * Cloudflare R2等へ直接アップロードするためのトークンを発行します。
 */
@RestController
@RequestMapping("/v1/market/items")
class UploadTokenController(
    private val generateUploadToken: GenerateUploadToken,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/upload-token")
    fun getUploadToken(servletRequest: HttpServletRequest): ResponseEntity<UploadTokenResponse> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }
        val response = generateUploadToken(userId, "USER")
        return ResponseEntity.ok(response)
    }
}
