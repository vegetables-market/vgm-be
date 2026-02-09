package com.example.myapp.controller.user.security.mfa


import com.example.myapp.controller.common.getAppUser
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.login.LoginService
import com.example.myapp.repository.user.email.UserEmailRepository
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
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService,
    private val loginService: LoginService,
    private val userEmailRepository: UserEmailRepository
) {
    @PostMapping("/enable/start")
    fun startMfaSetup(request: HttpServletRequest): ResponseEntity<Any> {
        val (userId, _) = request.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))
        }

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
