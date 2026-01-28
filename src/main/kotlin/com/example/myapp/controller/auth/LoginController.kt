package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.LoginRequest
import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.SignupRequest

import com.example.myapp.service.auth.AuthService
import com.example.myapp.service.auth.EmailVerificationService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth")
class LoginController(
    private val authService: AuthService
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
        val finalRequest = if (deviceId != null) request.copy(device_id = deviceId) else request
        
        val response = authService.login(finalRequest, ipAddress, userAgent)
        
        if (response.status == "AUTHENTICATED" && response.flow_id != null) {
            val cookie = Cookie("vgm_session", response.flow_id)
            cookie.isHttpOnly = true
            cookie.maxAge = 30 * 24 * 60 * 60 // 30 days
            cookie.path = "/"
            servletResponse.addCookie(cookie)
        }
        
        return if (response.status == "AUTHENTICATED" || response.status == "VERIFICATION_REQUIRED" || response.status == "PASSWORD_REQUIRED" || response.status == "MFA_REQUIRED") {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/logout")
    fun logout(
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val deviceId = servletRequest.cookies?.find { it.name == "vgm_session" }?.value
        
        if (deviceId != null) {
            authService.logout(deviceId)
        }
        
        val cookie = Cookie("vgm_session", null)
        cookie.isHttpOnly = true
        cookie.maxAge = 0
        cookie.path = "/"
        servletResponse.addCookie(cookie)
        
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Logged out successfully"))
    }




}
