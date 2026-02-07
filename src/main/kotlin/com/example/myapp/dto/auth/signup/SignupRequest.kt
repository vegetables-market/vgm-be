package com.example.myapp.dto.auth.signup

/**
 * 新規登録（サインアップ）のリクエストデータ
 */
data class SignupRequest(
    val username: String,    // ログインID
    val email: String,       // メールアドレス
    val password: String,    // 生のパスワード
    val display_name: String, // 表示名
    val birth_year: Int? = null,
    val birth_month: Int? = null,
    val birth_day: Int? = null,
    val gender: String? = null, // "male", "female", "other"
    val flow_id: String? = null // 事前認証フローID (Optional)
)