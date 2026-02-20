package com.example.myapp.service.auth.oauth

import com.example.myapp.entity.auth.UserOAuthConnection
import com.example.myapp.repository.auth.UserOAuthConnectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * OAuth接続の管理を担当するサービス
 */
@Service
class OAuthConnectionService(
    private val userOAuthConnectionRepository: UserOAuthConnectionRepository
) {

    /**
     * プロバイダとプロバイダユーザーIDでOAuth接続を検索
     * @param provider プロバイダ名 (google, github など)
     * @param providerUserId プロバイダ側のユーザーID
     * @return OAuth接続 (存在しない場合null)
     */
    fun findConnection(provider: String, providerUserId: String): UserOAuthConnection? {
        return userOAuthConnectionRepository.findByProviderAndProviderUserId(provider, providerUserId)
    }

    /**
     * OAuth接続を作成
     * @param userId ユーザーID
     * @param provider プロバイダ名
     * @param providerUserId プロバイダ側のユーザーID
     * @param emailId メールID
     * @param displayName 表示名
     * @return 作成されたOAuth接続
     */
    @Transactional
    fun createConnection(
        userId: Int,
        provider: String,
        providerUserId: String,
        emailId: Long,
        displayName: String
    ): UserOAuthConnection {
        val connection = UserOAuthConnection(
            userId = userId,
            provider = provider,
            providerUserId = providerUserId,
            emailId = emailId,
            displayName = displayName,
            lastUsedAt = LocalDateTime.now()
        )
        return userOAuthConnectionRepository.save(connection)
    }

    /**
     * OAuth接続の最終使用日時を更新
     * @param connection 更新するOAuth接続
     */
    @Transactional
    fun updateLastUsed(connection: UserOAuthConnection) {
        connection.lastUsedAt = LocalDateTime.now()
        userOAuthConnectionRepository.save(connection)
    }

    /**
     * 孤立したOAuth接続を削除
     * ユーザーが削除されている場合など
     * @param connection 削除するOAuth接続
     */
    @Transactional
    fun deleteOrphanedConnection(connection: UserOAuthConnection) {
        userOAuthConnectionRepository.delete(connection)
    }

    /**
     * ユーザーIDでOAuth接続のリストを取得
     * @param userId ユーザーID
     * @return OAuth接続のリスト
     */
    fun findConnectionsByUserId(userId: Int): List<UserOAuthConnection> {
        return userOAuthConnectionRepository.findByUserId(userId)
    }
}
