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
                loginService.getUserById(verification.userId)
            } else {
                loginService.getUserByIdentifier(verification.email!!)
            }

            if (user != null) {
                val ipAddress = servletRequest.remoteAddr
                val userAgent = servletRequest.getHeader("User-Agent")
                
                // 初回認証完了時、MFAタイプが未設定ならEmail MFAを有効化
                val authStatus = userAuthStatusRepository.findByUserId(user.userId)
                if (authStatus?.primaryMfaType == null) {
                    mfaService.enableEmailMfa(user.userId)
                }

                val sessionKey = sessionService.createSession(user.userId, ipAddress, userAgent)
                
                val cookie = Cookie("vgm_session", sessionKey)
                cookie.isHttpOnly = true
                cookie.maxAge = 30 * 24 * 60 * 60
                cookie.path = "/"
                servletResponse.addCookie(cookie)
                
                val primaryEmail = userEmailRepository.findByUserIdAndIsPrimaryTrue(user.userId)?.email
                
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "is_registered" to true,
                    "user" to mapOf(
                        "user_id" to user.userId,
                        "display_name" to user.displayName,
                        "email" to primaryEmail,
                        "is_email_verified" to true
                    )
                ))
            } else {
                // 新規ユーザー: 登録フローへ
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "is_registered" to false,
                    "email" to verification.email!!,
                    "flow_id" to flowId,
                    "message" to "Email verified. Please proceed to registration."
                ))
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
