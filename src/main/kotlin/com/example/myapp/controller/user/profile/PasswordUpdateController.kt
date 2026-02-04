package com.example.myapp.controller.user.profile

import com.example.myapp.dto.user.profile.ChangePasswordRequest
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.UserAuthStatusRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/profile")
class PasswordUpdateController(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

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
     * パスワード変更
     */
    @PutMapping("/password")
    @Transactional
    fun changePassword(
        @RequestBody request: ChangePasswordRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "ユーザーが見つかりません"))

        val authStatus = userAuthStatusRepository.findByUserId(userId)

        // パスワードを持っているユーザーの場合は現在のパスワードを確認
        if (authStatus?.hasPassword == true) {
            if (request.currentPassword.isNullOrBlank()) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "現在のパスワードを入力してください"))
            }
            if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "現在のパスワードが正しくありません"))
            }
        }

        // 新しいパスワードのバリデーション
        if (request.newPassword.length < 8) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "パスワードは8文字以上で入力してください"))
        }

        // パスワードを更新
        user.passwordHash = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)

        // AuthStatusのhasPasswordを更新
        if (authStatus != null && !authStatus.hasPassword) {
            authStatus.hasPassword = true
            userAuthStatusRepository.save(authStatus)
        }

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "パスワードを変更しました"
        ))
    }
}
