package com.example.myapp.controller.user.mfa

import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.LoginService
import com.example.myapp.repository.user.UserEmailRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * MFA有効化開始（QRコード生成）用コントローラー
 */
@RestController
@RequestMapping("/v1/user/mfa")
class MfaStartController(
    private val mfaService: MfaService,
    private val userSessionRepository: UserSessionRepository,
    private val loginService: LoginService,
    private val userEmailRepository: UserEmailRepository
) {

    private fun getUserIdFromSession(request: HttpServletRequest): Int? {
        val sessionKey = request.cookies?.find { it.name == "vgm_session" }?.value
            ?: return null

        val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
            sessionKey,
            LocalDateTime.now()
        ) ?: return null

        return session.userId
    }

    /**
     * MFA有効化を開始（QRコード生成）
     */
    @PostMapping("/enable/start")
    fun startMfaSetup(request: HttpServletRequest): ResponseEntity<Any> {
        val userId = getUserIdFromSession(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))

        val user = loginService.getUserById(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "User not found"))

        val userEmail = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)?.email 
            ?: "${user.username}@vgm.com"

        return try {
            val response = mfaService.startMfaSetup(userId, userEmail)
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
