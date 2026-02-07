package com.example.myapp.service.user

import com.example.myapp.dto.user.oauth.OAuthConnectionResponse
import com.example.myapp.entity.auth.UserOAuthConnection
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.auth.UserOAuthConnectionRepository
import com.example.myapp.service.auth.FirebaseAuthService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserOAuthService(
    private val oauthConnectionRepository: UserOAuthConnectionRepository, // Keep for query/delete for now
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val firebaseAuthService: FirebaseAuthService,
    private val oauthConnectionService: com.example.myapp.service.auth.oauth.OAuthConnectionService,
    private val oauthUserService: com.example.myapp.service.auth.oauth.OAuthUserService
) {

    /**
     * OAuth連携一覧取得
     */
    fun getConnections(userId: Int): Map<String, Any> {
        val connections = oauthConnectionRepository.findByUserId(userId)
        val connectionList = connections.map { conn ->
            OAuthConnectionResponse(
                connectionId = conn.connectionId,
                provider = conn.provider,
                providerEmail = conn.displayName,  // displayNameをメールとして表示
                connectedAt = conn.createdAt
            )
        }

        // 利用可能なプロバイダー一覧 (Apple削除)
        val availableProviders = listOf(
            mapOf("id" to "google", "name" to "Google", "icon" to ""),
            mapOf("id" to "microsoft", "name" to "Microsoft", "icon" to ""),
            mapOf("id" to "github", "name" to "GitHub", "icon" to "")
        )

        return mapOf(
            "connections" to connectionList,
            "availableProviders" to availableProviders
        )
    }

    /**
     * OAuth連携解除
     */
    @Transactional
    fun disconnectOAuth(userId: Int, provider: String): Map<String, Any> {
        val connection = oauthConnectionRepository.findByUserIdAndProvider(userId, provider)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "連携が見つかりません")

        // パスワードがない場合、最後のログイン手段を削除できない
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        val connections = oauthConnectionRepository.findByUserId(userId)
        
        if (authStatus?.hasPassword != true && connections.size <= 1) {
            throw AppException(ErrorCode.INVALID_INPUT, "最低1つのログイン方法が必要です。パスワードを設定してから解除してください")
        }

        oauthConnectionRepository.delete(connection)

        return mapOf(
            "success" to true,
            "message" to "${getProviderDisplayName(provider)}との連携を解除しました"
        )
    }

    /**
     * OAuth連携追加
     */
    @Transactional
    fun addConnection(userId: Int, token: String, provider: String): Map<String, Any> {
        // トークン検証
        val verifiedToken = firebaseAuthService.verifyToken(token)
        val providerUserId = verifiedToken.uid
        val email = verifiedToken.email
        val name = verifiedToken.name

        // 既に連携済みかチェック (Service経由)
        val existingConnection = oauthConnectionService.findConnection(provider, providerUserId)
        
        if (existingConnection != null) {
            if (existingConnection.userId == userId) {
                // 既に自アカウントに連携済み
                return mapOf(
                    "success" to true,
                    "message" to "既に連携済みです"
                )
            } else {
                // 他のアカウントに連携済み
                throw AppException(ErrorCode.INVALID_INPUT, "このアカウントは既に使用されています")
            }
        }

        // メールレコードの確保 (Service経由)
        val emailRecord = oauthUserService.ensureEmailRecord(userId, email, provider, false)

        // 新規連携 (Service経由)
        oauthConnectionService.createConnection(
            userId = userId,
            provider = provider,
            providerUserId = providerUserId,
            emailId = emailRecord.emailId,
            displayName = name ?: email
        )

        return mapOf(
            "success" to true,
            "message" to "${getProviderDisplayName(provider)}と連携しました"
        )
    }

    private fun getProviderDisplayName(provider: String): String {
        return when (provider.lowercase()) {
            "google" -> "Google"
            "microsoft" -> "Microsoft"
            "github" -> "GitHub"
            else -> provider
        }
    }
}
