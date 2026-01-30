package com.example.myapp.controller.user.oauth

import com.example.myapp.repository.auth.UserOAuthConnectionRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.UserAuthStatusRepository
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
@RequestMapping("/v1/user/oauth")
class OAuthDeleteController(
    private val oauthConnectionRepository: UserOAuthConnectionRepository,
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

    private fun getProviderDisplayName(provider: String): String {
        return when (provider.lowercase()) {
            "google" -> "Google"
            "microsoft" -> "Microsoft"
            "github" -> "GitHub"
            "apple" -> "Apple"
            else -> provider
        }
    }

    /**
     * OAuth連携解除
     */
    @DeleteMapping("/connections/{provider}")
    @Transactional
    fun disconnectOAuth(
        @PathVariable provider: String,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val connection = oauthConnectionRepository.findByUserIdAndProvider(userId, provider)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "連携が見つかりません"))

        // パスワードがない場合、最後のログイン手段を削除できない
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        val connections = oauthConnectionRepository.findByUserId(userId)
        
        if (authStatus?.hasPassword != true && connections.size <= 1) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "最低1つのログイン方法が必要です。パスワードを設定してから解除してください"))
        }

        oauthConnectionRepository.delete(connection)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "${getProviderDisplayName(provider)}との連携を解除しました"
        ))
    }
}
