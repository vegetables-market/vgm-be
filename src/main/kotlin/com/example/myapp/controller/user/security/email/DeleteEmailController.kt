package com.example.myapp.controller.user.security.email

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.UserEmailService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * メールアドレス削除コントローラー
 * 指定されたメールアドレスを削除します。
 */
@RestController
@RequestMapping("/v1/user/emails")
class DeleteEmailController(
    private val userEmailService: UserEmailService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @DeleteMapping("/{emailId}")
    fun deleteEmail(
        @PathVariable emailId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val result = userEmailService.deleteEmail(userId, emailId)
        return ResponseEntity.ok(result)
    }
}
