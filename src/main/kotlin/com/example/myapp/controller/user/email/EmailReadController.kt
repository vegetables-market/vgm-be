package com.example.myapp.controller.user.email

import com.example.myapp.dto.user.email.EmailResponse
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.auth.UserSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/emails")
class EmailReadController(
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
     * メールアドレス一覧取得
     */
    @GetMapping
    fun getEmails(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val emails = userEmailRepository.findByUserId(userId)
        val emailList = emails.map { email ->
            EmailResponse(
                emailId = email.emailId,
                email = email.email,
                isPrimary = email.isPrimary,
                isVerified = email.isVerified,
                createdAt = email.createdAt
            )
        }

        return ResponseEntity.ok(mapOf(
            "emails" to emailList
        ))
    }
}
