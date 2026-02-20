package com.example.myapp.controller.user.session

import com.example.myapp.repository.auth.UserSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/sessions")
class SessionDeleteController(
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
     * 特定のセッションを無効化
     */
    @DeleteMapping("/{sessionId}")
    @Transactional
    fun revokeSession(
        @PathVariable sessionId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val targetSession = userSessionRepository.findById(sessionId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "セッションが見つかりません"))

        if (targetSession.userId != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "権限がありません"))
        }

        val currentSessionKey = getCurrentSessionKey(servletRequest)
        if (targetSession.sessionKey == currentSessionKey) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "現在のセッションは無効化できません。ログアウトを使用してください"))
        }

        targetSession.isRevoked = true
        userSessionRepository.save(targetSession)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "セッションを無効化しました"
        ))
    }
}
