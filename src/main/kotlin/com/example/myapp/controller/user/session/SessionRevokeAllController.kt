package com.example.myapp.controller.user.session

import com.example.myapp.repository.auth.UserSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/sessions")
class SessionRevokeAllController(
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

    private fun getCurrentSessionKey(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == "vgm_session" }?.value
    }

    /**
     * 他のすべてのセッションを無効化
     */
    @DeleteMapping
    @Transactional
    fun revokeAllOtherSessions(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val currentSessionKey = getCurrentSessionKey(servletRequest)

        val sessions = userSessionRepository.findByUserId(userId)
        var revokedCount = 0

        sessions.forEach { session ->
            if (session.sessionKey != currentSessionKey && !session.isRevoked) {
                session.isRevoked = true
                userSessionRepository.save(session)
                revokedCount++
            }
        }

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "${revokedCount}件のセッションを無効化しました",
            "revokedCount" to revokedCount
        ))
    }
}
