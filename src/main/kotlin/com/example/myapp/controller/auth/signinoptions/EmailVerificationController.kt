package com.example.myapp.controller.auth.signinoptions

import com.example.myapp.dto.auth.signinoptions.VerifyEmailRequest
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.service.auth.LoginService
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.SessionService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth")
class EmailVerificationController(
    private val loginService: LoginService,
    private val mfaService: MfaService,
    private val sessionService: SessionService,
    private val emailVerificationService: EmailVerificationService,
    private val userAuthStatusRepository: com.example.myapp.repository.auth.UserAuthStatusRepository,
    private val userEmailRepository: com.example.myapp.repository.user.UserEmailRepository
) {

    @PostMapping("/verify-email")
    fun verifyEmail(@RequestBody request: VerifyEmailRequest): ResponseEntity<Map<String, Any>> {
        val success = emailVerificationService.verifyEmail(request.identifier, request.code)
        return if (success) {
            ResponseEntity.ok(mapOf("success" to true, "message" to "Email verified successfully"))
        } else {
            ResponseEntity.badRequest().body(mapOf("success" to false, "message" to "Invalid or expired code"))
        }
    }
}
