package com.example.myapp.controller.user.profile

import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.UserAuthStatusRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/profile")
class ProfileReadController(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
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
     * ユーザー情報取得（現在のユーザー名等）
     */
    @GetMapping("/me")
    fun getMyProfile(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "ユーザーが見つかりません"))

        val authStatus = userAuthStatusRepository.findByUserId(userId)

        return ResponseEntity.ok(mapOf(
            "userId" to user.userId,
            "username" to user.username,
            "hasPassword" to (authStatus?.hasPassword ?: false),
            "displayName" to (user.displayName ?: user.username),
            "role" to user.role
        ))
    }
}
