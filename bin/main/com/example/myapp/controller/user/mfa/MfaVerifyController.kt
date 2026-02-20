package com.example.myapp.controller.user.mfa

import com.example.myapp.dto.user.mfa.MfaVerifyRequest
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.auth.MfaService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * MFA認証コード検証用コントローラー
 */
@RestController
@RequestMapping("/v1/user/mfa")
class MfaVerifyController(
    private val mfaService: MfaService,
    private val userSessionRepository: UserSessionRepository
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
     * TOTPコードを検証してMFAを有効化
     */
    @PostMapping("/enable/verify")
    fun verifyAndEnable(
        @RequestBody body: MfaVerifyRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val userId = getUserIdFromSession(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))

        return try {
            val response = mfaService.verifyAndEnable(userId, body.code)
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
