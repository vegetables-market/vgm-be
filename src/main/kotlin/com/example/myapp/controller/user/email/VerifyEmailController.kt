package com.example.myapp.controller.user.email

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.email.VerifyEmailRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.user.UserEmailService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * メールアドレス確認コントローラー
 * 送信された確認コードを検証し、メールアドレスを有効化します。
 */
@RestController
@RequestMapping("/v1/user/emails")
class VerifyEmailController(
    private val userEmailService: UserEmailService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/verify")
    fun verifyAddEmail(
        @RequestBody request: VerifyEmailRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val result = userEmailService.verifyAddEmail(userId, request)
        return ResponseEntity.ok(result)
    }
}
