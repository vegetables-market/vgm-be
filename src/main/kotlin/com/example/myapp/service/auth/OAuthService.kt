package com.example.myapp.service.auth

import com.example.myapp.entity.user.User
import com.example.myapp.service.auth.oauth.OAuthConnectionService
import com.example.myapp.service.auth.oauth.OAuthUserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * OAuth サービスの Facade
 * 外部に統一されたインターフェースを提供し、内部では各専門サービスに委譲
 */
@Service
class OAuthService(
    private val oauthConnectionService: OAuthConnectionService,
    private val oauthUserService: OAuthUserService,
    private val sessionService: SessionService
) {

    /**
     * OAuth2ユーザーを処理してセッションを作成
     * @param email メールアドレス
     * @param name 表示名
     * @param provider プロバイダ名 (google, github など)
     * @param providerUserId プロバイダ側のユーザーID
     * @return セッションキー
     */
    @Transactional
    fun processOAuth2User(email: String, name: String, provider: String, providerUserId: String): String {
        // 1. Check if OAuth connection exists
        val existingConnection = oauthConnectionService.findConnection(provider, providerUserId)
        
        var user: User? = null
        var isNewUser = false

        if (existingConnection != null) {
            // Existing OAuth connection - get user
            user = oauthUserService.findUserByEmail(email)
            
            if (user != null && user.userId == existingConnection.userId) {
                // Update last used
                oauthConnectionService.updateLastUsed(existingConnection)
            } else {
                // ユーザーが削除されている、または不整合がある場合
                // 孤立したOAuth接続を削除して新規作成フローへ
                oauthConnectionService.deleteOrphanedConnection(existingConnection)
                user = null
            }
        }

        if (user == null) {
            // Check if user exists by email
            user = oauthUserService.findUserByEmail(email)

            if (user == null) {
                // Create new user (OAuth only - no password)
                isNewUser = true
                user = oauthUserService.createOAuthUser(email, name, provider)
            }

            // Create email record if not exists
            val emailRecord = oauthUserService.ensureEmailRecord(user.userId, email, provider, isNewUser)

            // Create OAuth connection
            oauthConnectionService.createConnection(
                userId = user.userId,
                provider = provider,
                providerUserId = providerUserId,
                emailId = emailRecord.emailId,
                displayName = name
            )
        }

        // Update login info
        oauthUserService.updateLoginInfo(user.userId, provider)

        // Create Session via SessionService
        return sessionService.createSession(user.userId, null, "OAuth2: $provider")
    }
}
