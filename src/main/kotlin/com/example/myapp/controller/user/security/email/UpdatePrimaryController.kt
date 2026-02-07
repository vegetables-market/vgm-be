package com.example.myapp.controller.user.security.email

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.user.UserEmailService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * プライマリメールアドレス変更コントローラー
 * 指定されたメールアドレスをプライマリに設定します。
 */
@RestController
@RequestMapping("/v1/user/emails")
class UpdatePrimaryController(
    private val userEmailService: UserEmailService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PutMapping("/{emailId}/primary")
    fun setPrimaryEmail(
        @PathVariable emailId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val result = userEmailService.setPrimaryEmail(userId, emailId)
        return ResponseEntity.ok(result)
    }
}
