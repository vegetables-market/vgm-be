package com.example.myapp.controller.user.security.email

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.security.email.EmailResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.UserEmailService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * メールアドレス一覧取得コントローラー
 * ユーザーに紐付くメールアドレスの一覧を取得します。
 */
@RestController
@RequestMapping("/v1/user/emails")
class GetEmailsController(
    private val userEmailService: UserEmailService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @GetMapping
    fun getEmails(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, List<EmailResponse>>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val emails = userEmailService.getEmails(userId)
        return ResponseEntity.ok(mapOf("emails" to emails))
    }
}
