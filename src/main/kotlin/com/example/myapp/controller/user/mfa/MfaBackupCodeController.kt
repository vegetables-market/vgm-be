package com.example.myapp.controller.user.mfa

import com.example.myapp.dto.user.mfa.BackupCodesResponse
import com.example.myapp.dto.user.mfa.RegenerateCodesRequest
import com.example.myapp.controller.common.getAppUser
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.auth.MfaService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * バックアップコード再生成用コントローラー
 */
@RestController
@RequestMapping("/v1/user/mfa")
class MfaBackupCodeController(
    private val mfaService: MfaService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    @PostMapping("/regenerate-backup-codes")
    fun regenerateBackupCodes(
        @RequestBody body: RegenerateCodesRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val (userId, _) = request.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))
        }

        return try {
            val codes = mfaService.regenerateBackupCodes(userId, body.password)
            ResponseEntity.ok(BackupCodesResponse(backupCodes = codes))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
