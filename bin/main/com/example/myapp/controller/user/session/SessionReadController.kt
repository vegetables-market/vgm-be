package com.example.myapp.controller.user.session

import com.example.myapp.dto.user.session.SessionResponse
import com.example.myapp.repository.auth.UserSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/sessions")
class SessionReadController(
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
     * アクティブセッション一覧取得
     */
    @GetMapping
    fun getSessions(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val currentSessionKey = getCurrentSessionKey(servletRequest)

        val sessions = userSessionRepository.findByUserId(userId)
            .filter { !it.isRevoked && it.expiresAt.isAfter(LocalDateTime.now()) }
            .map { session ->
                SessionResponse(
                    sessionId = session.sessionId,
                    deviceInfo = session.deviceName,
                    ipAddress = session.ipAddress,
                    createdAt = session.createdAt,
                    lastActiveAt = session.lastAccessedAt,
                    expiresAt = session.expiresAt,
                    isCurrent = session.sessionKey == currentSessionKey
                )
            }
            .sortedByDescending { it.isCurrent }  // 現在のセッションを先頭に

        return ResponseEntity.ok(mapOf(
            "sessions" to sessions
        ))
    }
}
