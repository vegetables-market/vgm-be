package com.example.myapp.dto.auth.login

/**
 * ユーザー識別子チェックリクエスト
 */
data class CheckUserRequest(
    val identifier: String
)

/**
 * 次の認証ステップレスポンス
 */
data class CheckUserResponse(
    val nextStep: String, // "password" | "email_otp"
    val identifier: String,
    val flowId: String? = null
)
