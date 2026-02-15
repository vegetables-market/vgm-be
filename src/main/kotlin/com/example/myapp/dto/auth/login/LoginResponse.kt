package com.example.myapp.dto.auth.login

/**
 * 共通ログインレスポンスDTO
 *
 * 認証成功時、またはMFA等の追加ステップが必要な場合に返却される。
 *
 * @property status 認証ステータス (AUTHENTICATED, MFA_REQUIRED, etc)
 * @property flowId 追加ステップが必要な場合のフローID
 * @property user ログインユーザー情報 (認証成功時)
 * @property mfaToken 必要に応じて発行される一時トークン
 * Used in: LoginController, GoogleAuthController, etc.
 */

data class LoginResponse(
    val status: String,
    val user: UserInfo?,
    val message: String? = null,
    val requireVerification: Boolean = false,
    val flowId: String? = null,
    val maskedEmail: String? = null,
    val mfaToken: String? = null, // MFA検証用の一時トークン
    val mfaType: String? = null, // "TOTP", "EMAIL" etc.
    val expiresAt: String? = null, // 認証コードの有効期限 (ISO 8601)
    val nextResendAt: String? = null // 認証コード再送可能時刻 (ISO 8601)
)

data class UserInfo(
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String? = null,
    val isEmailVerified: Boolean
)
