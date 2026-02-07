package com.example.myapp.controller.user.oauth

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.user.UserOAuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AddOAuthRequest(
    val token: String,
    val provider: String
)

/**
 * OAuth連携追加コントローラー
 */
@RestController
@RequestMapping("/v1/user/oauth")
class AddOAuthConnectionController(
    private val userOAuthService: UserOAuthService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/connections")
    fun addConnection(
        @RequestBody request: AddOAuthRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }

        val result = userOAuthService.addConnection(userId, request.token, request.provider)
        return ResponseEntity.ok(result)
    }
}
