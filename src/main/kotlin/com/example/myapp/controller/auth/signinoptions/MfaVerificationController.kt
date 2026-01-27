package com.example.myapp.controller.auth.signinoptions

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.service.auth.AuthService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth")
class MfaVerificationController(
    private val authService: AuthService
) {

    @PostMapping("/verify-mfa")
    fun verifyMfa(
        @RequestBody request: com.example.myapp.dto.auth.VerifyMfaLoginRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        return try {
            val ipAddress = servletRequest.remoteAddr
            val userAgent = servletRequest.getHeader("User-Agent")
            
            val response = authService.verifyMfa(request.mfa_token, request.code, ipAddress, userAgent)
            
            // flow_idにはセッションキーが入っている
            val sessionKey = response.flow_id
            
            if (sessionKey != null) {
                val cookie = Cookie("vgm_session", sessionKey)
                cookie.isHttpOnly = true
                cookie.maxAge = 30 * 24 * 60 * 60 // 30 days
                cookie.path = "/"
                servletResponse.addCookie(cookie)
            }
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            // エラー時は401または400
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}
