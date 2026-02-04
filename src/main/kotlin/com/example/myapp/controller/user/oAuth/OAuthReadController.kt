package com.example.myapp.controller.user.oauth

import com.example.myapp.dto.user.oauth.OAuthConnectionResponse
import com.example.myapp.repository.auth.UserOAuthConnectionRepository
import com.example.myapp.repository.auth.UserSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user/oauth")
class OAuthReadController(
    private val oauthConnectionRepository: UserOAuthConnectionRepository,
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
     * OAuth連携一覧取得
     */
    @GetMapping("/connections")
    fun getConnections(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val connections = oauthConnectionRepository.findByUserId(userId)
        val connectionList = connections.map { conn ->
            OAuthConnectionResponse(
                connectionId = conn.connectionId,
                provider = conn.provider,
                providerEmail = conn.displayName,  // displayNameをメールとして表示
                connectedAt = conn.createdAt
            )
        }

        // 利用可能なプロバイダー一覧
        val availableProviders = listOf(
            mapOf("id" to "google", "name" to "Google", "icon" to ""),
            mapOf("id" to "microsoft", "name" to "Microsoft", "icon" to ""),
            mapOf("id" to "github", "name" to "GitHub", "icon" to ""),

        )

        return ResponseEntity.ok(mapOf(
            "connections" to connectionList,
            "availableProviders" to availableProviders
        ))
    }
}
