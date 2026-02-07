package com.example.myapp.controller.auth

import com.example.myapp.service.auth.SessionService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class LogoutController(
    private val sessionService: SessionService,
    private val appCookieService: com.example.myapp.service.auth.AppCookieService
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
        
        appCookieService.removeSessionCookie(servletResponse)
        
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Logged out successfully"))
    }
}
