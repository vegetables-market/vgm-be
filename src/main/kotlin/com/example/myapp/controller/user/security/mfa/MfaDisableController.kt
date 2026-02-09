package com.example.myapp.controller.user.security.mfa

import com.example.myapp.dto.user.security.mfa.MfaDisableRequest
import com.example.myapp.controller.common.getAppUser
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.auth.MfaService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * MFA無効化用コントローラー
 */
@RestController
@RequestMapping("/v1/user/mfa")
class MfaDisableController(
    private val mfaService: MfaService,
    private val appCookieService: AppCookieService,
    private val sessionService: SessionService
) {

    @PostMapping("/disable")
    fun disableMfa(
        @RequestBody body: MfaDisableRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val (userId, _) = request.getAppUser(appCookieService, sessionService)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))
        }

        return try {
            val success = mfaService.disableMfa(userId, body.code, body.password)
            ResponseEntity.ok(mapOf("success" to success))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
