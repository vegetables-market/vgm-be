package com.example.myapp.service.auth

import com.example.myapp.entity.user.User
import com.example.myapp.service.auth.oauth.OAuthConnectionService
import com.example.myapp.service.auth.oauth.OAuthUserService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.auth.common.DataMergeService
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
    private val sessionService: SessionService,
    private val dataMergeService: DataMergeService
) {

    /**
     * OAuth2ユーザーを処理してセッションを作成
     * @param email メールアドレス
     * @param name 表示名
     * @param provider プロバイダ名 (google, github など)
     * @param providerUserId プロバイダ側のユーザーID
     * @param guestId ゲストID (データ統合用)
     * @return セッションキー
     */
    @Transactional
    fun processOAuth2User(email: String, name: String, provider: String, providerUserId: String, guestId: String? = null): String {
        // 1. OAuth接続が存在するか確認
        val existingConnection = oauthConnectionService.findConnection(provider, providerUserId)
        
        var user: User? = null
        var isNewUser = false

        if (existingConnection != null) {
            // 既存のOAuth接続 - ユーザーを取得
            user = oauthUserService.findUserByEmail(email)
            
            if (user != null && user.userId == existingConnection.userId) {
                // 最終使用日時を更新
                oauthConnectionService.updateLastUsed(existingConnection)
            } else {
                // ユーザーが削除されている、または不整合がある場合
                // 孤立したOAuth接続を削除して新規作成フローへ
                oauthConnectionService.deleteOrphanedConnection(existingConnection)
                user = null
            }
        }

        if (user == null) {
            // メールアドレスでユーザーが存在するか確認
            user = oauthUserService.findUserByEmail(email)

            if (user == null) {
                // 新規ユーザー作成 (OAuthのみ - パスワードなし)
                isNewUser = true
                user = oauthUserService.createOAuthUser(email, name, provider)
            }

            // メールレコードが存在しない場合は作成
            val emailRecord = oauthUserService.ensureEmailRecord(user.userId, email, provider, isNewUser)

            // OAuth接続を作成
            oauthConnectionService.createConnection(
                userId = user.userId,
                provider = provider,
                providerUserId = providerUserId,
                emailId = emailRecord.emailId,
                displayName = name
            )
        }

        // ログイン情報を更新
        oauthUserService.updateLoginInfo(user.userId, provider)

        // SessionService経由でセッションを作成
        val sessionKey = sessionService.createSession(user.userId, null, "OAuth2: $provider")
        
        // ゲストデータ統合
        if (guestId != null) {
            dataMergeService.mergeGuestData(user.userId, guestId)
        }
        
        return sessionKey
    }
}
