package com.example.myapp.controller.user.security.mfa


import com.example.myapp.controller.common.getAppUser
import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.auth.MfaService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * MFA状態確認用コントローラー
 */
@RestController
@RequestMapping("/v1/user/mfa")
class MfaStatusController(
    private val mfaService: MfaService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    @GetMapping("/status")
    fun getMfaStatus(request: HttpServletRequest): ResponseEntity<Any> {
        val (userId, _) = request.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))
        }

        val status = mfaService.getMfaStatus(userId)
        return ResponseEntity.ok(status)
    }
}
