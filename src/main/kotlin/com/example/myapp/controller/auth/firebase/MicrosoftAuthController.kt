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

data class MicrosoftLoginRequest(
    val token: String
)

@RestController
@RequestMapping("/v1/auth")
class MicrosoftAuthController(
    private val firebaseAuthService: FirebaseAuthService
) {

    @PostMapping("/microsoft")
    fun login(@RequestBody request: MicrosoftLoginRequest, servletRequest: HttpServletRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return firebaseAuthService.processLogin(request.token, "microsoft", servletRequest, response)
    }
}
