package com.example.myapp.controller.user

import com.example.myapp.dto.user.*
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.AuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * MFA（多要素認証）コントローラー
 * 既存の vgm_session Cookie ベースの認証を使用
 */
@RestController
@RequestMapping("/v1/user/mfa")
class MfaController(
    private val mfaService: MfaService,
    private val userSessionRepository: UserSessionRepository,
    private val authService: AuthService
) {

    /**
     * セッションCookieからユーザーIDを取得
     * @return ユーザーID。認証されていない場合はnull
     */
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

        val user = authService.getUserById(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "User not found"))

        val userEmail = user.email ?: "${user.username}@vgm.com"

        return try {
            val response = mfaService.startMfaSetup(userId, userEmail)
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
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

    /**
     * MFAを無効化
     */
    @PostMapping("/disable")
    fun disableMfa(
        @RequestBody body: MfaDisableRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val userId = getUserIdFromSession(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))

        return try {
            val success = mfaService.disableMfa(userId, body.code, body.password)
            ResponseEntity.ok(mapOf("success" to success))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * バックアップコードを再生成
     */
    @PostMapping("/regenerate-backup-codes")
    fun regenerateBackupCodes(
        @RequestBody body: RegenerateCodesRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val userId = getUserIdFromSession(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))

        return try {
            val codes = mfaService.regenerateBackupCodes(userId, body.password)
            ResponseEntity.ok(BackupCodesResponse(backupCodes = codes))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * MFA状態を取得
     */
    @GetMapping("/status")
    fun getMfaStatus(request: HttpServletRequest): ResponseEntity<Any> {
        val userId = getUserIdFromSession(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))

        val status = mfaService.getMfaStatus(userId)
        return ResponseEntity.ok(status)
    }
}
