package com.example.myapp.controller.auth

import com.example.myapp.service.auth.SessionService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class LogoutController(
    private val sessionService: SessionService
) {

    @PostMapping("/logout")
    fun logout(
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val deviceId = servletRequest.cookies?.find { it.name == "vgm_session" }?.value
        
        if (deviceId != null) {
            sessionService.logout(deviceId)
        }
        
        val cookie = Cookie("vgm_session", null)
        cookie.isHttpOnly = true
        cookie.maxAge = 0
        cookie.path = "/"
        servletResponse.addCookie(cookie)
        
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Logged out successfully"))
    }
}
