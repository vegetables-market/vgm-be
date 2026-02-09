package com.example.myapp.controller.user.security.oauth

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.UserOAuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * OAuth連携一覧取得コントローラー
 */
@RestController
@RequestMapping("/v1/user/oauth")
class GetOAuthConnectionsController(
    private val userOAuthService: UserOAuthService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @GetMapping("/connections")
    fun getConnections(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val result = userOAuthService.getConnections(userId)
        return ResponseEntity.ok(result)
    }
}
