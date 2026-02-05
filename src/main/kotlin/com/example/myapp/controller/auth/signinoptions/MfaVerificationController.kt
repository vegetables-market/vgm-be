package com.example.myapp.controller.auth.signinoptions

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.service.auth.LoginService
import com.example.myapp.service.auth.MfaService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth")
class MfaVerificationController(
    private val mfaService: MfaService,
    private val loginService: LoginService,
    private val sensitiveActionService: com.example.myapp.service.auth.SensitiveActionService // Inject
) {

    @PostMapping("/verify-mfa")
    fun verifyMfa(
        @RequestBody request: com.example.myapp.dto.auth.signinoptions.VerifyMfaLoginRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any?>> { // Return type changed to generic Map to support ActionToken
        return try {
            val ipAddress = servletRequest.remoteAddr
            val userAgent = servletRequest.getHeader("User-Agent")
            
            // 1. Verify MFA
            val userId = mfaService.verifyLoginMfa(request.mfa_token, request.code)
            
            // Action Token 発行 (もしアクション指定があれば)
            if (request.action != null) {
                val actionToken = sensitiveActionService.issueActionToken(userId, request.action)
                
                return ResponseEntity.ok(mapOf(
                    "success" to true,
                    "action_token" to actionToken,
                    // LoginResponse互換のuserオブジェクトなども必要なら返す
                    // 現状のTOTPVerificationはLoginResponseを期待しているため、Userは返した方が親切
                    "user" to mapOf("user_id" to userId), // Minimal fields
                    "action" to request.action
                ))
            }

            // 2. Complete Login (Normal Flow)
            val response = loginService.completeLogin(userId, ipAddress, userAgent)
            
            // flow_idにはセッションキーが入っている
            val sessionKey = response.flow_id
            
            if (sessionKey != null) {
                val cookie = Cookie("vgm_session", sessionKey)
                cookie.isHttpOnly = true
                cookie.maxAge = 30 * 24 * 60 * 60 // 30 days
                cookie.path = "/"
                servletResponse.addCookie(cookie)
            }
            
            // Convert LoginResponse to Map for consistency with this method signature
            val responseMap = mapOf(
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
            // エラー時は401または400
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}
