package com.example.myapp.controller.auth.login

import com.example.myapp.dto.auth.login.LoginRequest
import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.GuestSessionService
import com.example.myapp.service.auth.login.LoginUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class LoginController(
    private val loginUser: LoginUser,
    private val appCookieService: AppCookieService
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val ipAddress = servletRequest.remoteAddr
        val userAgent = servletRequest.getHeader("User-Agent")
        val deviceId = servletRequest.cookies?.find { it.name == "vgm_session" }?.value
        val finalRequest = if (deviceId != null) request.copy(deviceId = deviceId) else request

        val guestId = servletRequest.cookies?.find { it.name == GuestSessionService.GUEST_COOKIE_NAME }?.value

        val response = loginUser(finalRequest, ipAddress, userAgent, guestId)

        if (response.status == "AUTHENTICATED" && response.flowId != null) {
            appCookieService.addSessionCookie(servletResponse, response.flowId)
        }

        return if (response.status == "AUTHENTICATED" || response.status == "VERIFICATION_REQUIRED" || response.status == "PASSWORD_REQUIRED" || response.status == "MFA_REQUIRED") {
            ResponseEntity.ok(response)
        } else if (response.status == "INVALID_CREDENTIALS") {
            throw AppException(ErrorCode.AUTH_INVALID_CREDENTIALS)
        } else {
            // Fallback for other errors
            throw AppException(ErrorCode.AUTH_INVALID_CREDENTIALS)
        }
    }
}