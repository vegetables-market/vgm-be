package com.example.myapp.controller.user.profile

import com.example.myapp.dto.user.profile.ChangeUsernameRequest
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
class ProfileUpdateController(
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
     * ユーザー名変更
     */
    @PutMapping("/username")
    @Transactional
    fun changeUsername(
        @RequestBody request: ChangeUsernameRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "ユーザーが見つかりません"))

        val authStatus = userAuthStatusRepository.findByUserId(userId)

        // パスワードを持っているユーザーの場合はパスワード確認
        if (authStatus?.hasPassword == true) {
            if (request.password.isNullOrBlank()) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "パスワードを入力してください"))
            }
            if (!passwordEncoder.matches(request.password, user.passwordHash)) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "パスワードが正しくありません"))
            }
        }

        // ユーザー名のバリデーション
        val newUsername = request.newUsername.trim()
        if (newUsername.length < 3) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "ユーザー名は3文字以上で入力してください"))
        }
        if (newUsername.length > 20) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "ユーザー名は20文字以下で入力してください"))
        }
        if (!newUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "ユーザー名は英数字とアンダースコアのみ使用できます"))
        }

        // 重複チェック
        if (userRepository.existsByUsername(newUsername) && user.username != newUsername) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "このユーザー名は既に使用されています"))
        }

        // ユーザー名を更新
        user.username = newUsername
        userRepository.save(user)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "ユーザー名を変更しました",
            "username" to newUsername
        ))
    }
}
