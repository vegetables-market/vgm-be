package com.example.myapp.dto.auth.signup

/**
 * 新規登録（サインアップ）のリクエストデータ
 */
data class SignupRequest(
    val username: String,    // ログインID
    val email: String,       // メールアドレス
    val password: String,    // 生のパスワード
    val displayName: String, // 表示名
    val birthYear: Int? = null,
    val birthMonth: Int? = null,
    val birthDay: Int? = null,
    val gender: String? = null, // "male", "female", "other"
    val flowId: String? = null // 事前認証フローID (Optional)
)