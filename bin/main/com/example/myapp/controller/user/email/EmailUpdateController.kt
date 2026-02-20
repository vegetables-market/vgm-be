package com.example.myapp.controller.user.email

import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.auth.UserSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/emails")
class EmailUpdateController(
    private val userEmailRepository: UserEmailRepository,
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
     * プライマリメールアドレスの変更
     */
    @PutMapping("/{emailId}/primary")
    @Transactional
    fun setPrimaryEmail(
        @PathVariable emailId: Long,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val targetEmail = userEmailRepository.findById(emailId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "メールアドレスが見つかりません"))

        if (targetEmail.userId != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "権限がありません"))
        }

        if (!targetEmail.isVerified) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "認証済みのメールアドレスのみプライマリに設定できます"))
        }

        // 現在のプライマリを解除
        val currentPrimary = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
        if (currentPrimary != null) {
            currentPrimary.isPrimary = false
            userEmailRepository.save(currentPrimary)
        }

        // 新しいプライマリを設定
        targetEmail.isPrimary = true
        userEmailRepository.save(targetEmail)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "プライマリメールアドレスを変更しました"
        ))
    }
}
