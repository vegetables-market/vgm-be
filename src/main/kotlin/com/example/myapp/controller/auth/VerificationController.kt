package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.AuthMethod
import com.example.myapp.dto.auth.VerifyAuthRequest
import com.example.myapp.service.auth.LoginService
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.SensitiveActionService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.UserEmailRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class VerificationController(
    private val loginService: LoginService,
    private val mfaService: MfaService,
    private val sessionService: SessionService,
    private val emailVerificationService: EmailVerificationService,
    private val sensitiveActionService: SensitiveActionService,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val userEmailRepository: UserEmailRepository
) {

    @PostMapping("/verify")
    fun verify(
        @RequestBody request: VerifyAuthRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val userId = when (request.method) {
                AuthMethod.EMAIL -> verifyEmail(request.identifier, request.code)
                AuthMethod.TOTP -> verifyTotp(request.identifier, request.code)
            }

            if (userId == null) {
                return ResponseEntity.badRequest().body(mapOf("success" to false, "message" to "Invalid or expired code"))
            }

            val user = loginService.getUserById(userId) ?: return ResponseEntity.badRequest().build()

            // Action Token Flow
            if (request.action != null) {
                val actionToken = sensitiveActionService.issueActionToken(userId, request.action)
                return ResponseEntity.ok(mapOf(
                    "success" to true,
                    "action_token" to actionToken,
                    "user" to mapOf("user_id" to userId),
                    "action" to request.action
                ))
            }

            // Login Flow
            val ipAddress = servletRequest.remoteAddr
            val userAgent = servletRequest.getHeader("User-Agent")

            // Enable Email MFA if primary type is null (Logic from EmailVerificationController)
            if (request.method == AuthMethod.EMAIL) {
                val authStatus = userAuthStatusRepository.findByUserId(userId)
                if (authStatus?.primaryMfaType == null) {
                    mfaService.enableEmailMfa(userId)
                }
            }

            // Create Session
            // Note: LoginService.completeLogin does similar things but specific to LoginResponse.
            // Here we want to share logic. 
            // For now, let's execute the logic directly here closely matching EmailVerificationController
            // but for TOTP, MfaVerificationController called loginService.completeLogin.
            // We should aim for consistency. loginService.completeLogin is more robust for Login.
            
            val response = loginService.completeLogin(userId, ipAddress, userAgent)
            
            val sessionKey = response.flow_id // completeLogin puts session key in flow_id
            if (sessionKey != null) {
                val cookie = Cookie("vgm_session", sessionKey)
                cookie.isHttpOnly = true
                cookie.maxAge = 30 * 24 * 60 * 60 // 30 days
                cookie.path = "/"
                servletResponse.addCookie(cookie)
            }

             val responseMap = mapOf(
                "success" to true,
                "status" to response.status,
                "user" to response.user,
                "flow_id" to response.flow_id,
                "mfa_token" to response.mfa_token,
                "mfa_type" to response.mfa_type,
                "masked_email" to response.masked_email,
                "require_verification" to response.require_verification
            )
            ResponseEntity.ok(responseMap)

        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("success" to false, "message" to e.message))
        }
    }

    private fun verifyEmail(flowId: String, code: String): Int? {
        val verification = emailVerificationService.verifyByFlowId(flowId, code) ?: return null
        return verification.userId ?: loginService.getUserByIdentifier(verification.email!!)?.userId
    }

    private fun verifyTotp(mfaToken: String, code: String): Int? {
        // verifyLoginMfa throws exception on failure
        return try {
            mfaService.verifyLoginMfa(mfaToken, code)
        } catch (e: Exception) {
            null
        }
    }
}
