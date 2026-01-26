package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.LoginRequest
import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.SignupRequest
import com.example.myapp.dto.auth.VerifyEmailRequest
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
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        val ipAddress = servletRequest.remoteAddr
        val userAgent = servletRequest.getHeader("User-Agent")
        val deviceId = servletRequest.cookies?.find { it.name == "vgm_session" }?.value
        val finalRequest = if (deviceId != null) request.copy(device_id = deviceId) else request
        
        val response = authService.login(finalRequest, ipAddress, userAgent)
        
        return if (response.status == "AUTHENTICATED" || response.status == "VERIFICATION_REQUIRED" || response.status == "PASSWORD_REQUIRED") {
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

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<Any> {
        return try {
            val response = authService.signup(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to e.message))
        }
    }

    /**
     * 認証コード再送信
     */
    @PostMapping("/resend-code")
    fun resendCode(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val flowId = request["flow_id"] ?: return ResponseEntity.badRequest().build()
        
        val newFlowId = emailVerificationService.resendVerificationEmail(flowId)
        
        return if (newFlowId != null) {
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Verification code resent",
                "flow_id" to newFlowId
            ))
        } else {
            ResponseEntity.badRequest().body(mapOf("success" to false, "message" to "Invalid flow_id"))
        }
    }

    @PostMapping("/verify-challenge")
    fun verifyChallenge(
        @RequestBody request: Map<String, String>,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val flowId = request["flow_id"] ?: return ResponseEntity.badRequest().build()
        val code = request["code"] ?: return ResponseEntity.badRequest().build()
        
        val verification = emailVerificationService.verifyByFlowId(flowId, code)
        
        return if (verification != null) {
            val user = if (verification.userId != null) {
                authService.getUserById(verification.userId)
            } else {
                authService.getUserByIdentifier(verification.email!!)
            }

            if (user != null) {
                val ipAddress = servletRequest.remoteAddr
                val userAgent = servletRequest.getHeader("User-Agent")
                val sessionKey = authService.createSession(user.userId, ipAddress, userAgent)
                
                val cookie = Cookie("vgm_session", sessionKey)
                cookie.isHttpOnly = true
                cookie.maxAge = 30 * 24 * 60 * 60
                cookie.path = "/"
                servletResponse.addCookie(cookie)
                
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "user" to mapOf(
                        "user_id" to user.userId,
                        "display_name" to user.displayName,
                        "email" to user.email,
                        "is_email_verified" to true
                    )
                ))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "User not found after verification"))
            }
        } else {
            ResponseEntity.badRequest().body(mapOf("success" to false, "message" to "Invalid or expired code"))
        }
    }

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
