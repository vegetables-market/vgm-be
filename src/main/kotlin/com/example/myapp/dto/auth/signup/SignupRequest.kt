package com.example.myapp.dto.auth.signup

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * ユーザー新規登録リクエストDTO
 *
 * @property username ログインID
 * @property email メールアドレス
 * @property password 生のパスワード
 * @property displayName 表示名
 * @property birthYear 生年 (任意)
 * @property birthMonth 生月 (任意)
 * @property birthDay 生日 (任意)
 * @property gender 性別 (male, female, other) (任意)
 * @property flowId 事前認証フローID (任意)
 * Used in: [com.example.myapp.controller.auth.signup.SignupController]
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
    @JsonProperty("flow_id")
    val flowId: String? = null, // 事前認証フローID (Optional)
    @JsonProperty("oauth_token")
    val oauthToken: String? = null,
    @JsonProperty("oauth_provider")
    val oauthProvider: String? = null
)