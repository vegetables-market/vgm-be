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
    private val firebaseAuthService: FirebaseAuthService
) {

    @PostMapping("/google")
    fun login(@RequestBody request: GoogleLoginRequest, servletRequest: HttpServletRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return firebaseAuthService.processLogin(request.token, "google", servletRequest, response)
    }
}
