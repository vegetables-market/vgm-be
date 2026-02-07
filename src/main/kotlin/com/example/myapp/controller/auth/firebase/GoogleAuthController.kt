package com.example.myapp.controller.auth.firebase

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.service.auth.FirebaseAuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class GoogleLoginRequest(
    val token: String
)

@RestController
@RequestMapping("/v1/auth")
class GoogleAuthController(
    private val firebaseAuthService: FirebaseAuthService,
    private val appCookieService: com.example.myapp.service.auth.AppCookieService
) {

    @PostMapping("/google")
    fun login(@RequestBody request: GoogleLoginRequest, servletRequest: HttpServletRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        val guestId = servletRequest.cookies?.find { it.name == com.example.myapp.service.auth.GuestSessionService.GUEST_COOKIE_NAME }?.value
        val loginResponse = firebaseAuthService.processLogin(request.token, "google", guestId)
        
        if (loginResponse.status == "AUTHENTICATED" && loginResponse.flow_id != null) {
            appCookieService.addSessionCookie(response, loginResponse.flow_id)
        }
        
        return ResponseEntity.ok(loginResponse)
    }
}
