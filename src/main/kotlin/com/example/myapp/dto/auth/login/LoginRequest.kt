package com.example.myapp.dto.auth.login

/**
 * メールアドレス/パスワードログインリクエストDTO
 *
 * @property username メールアドレスまたはユーザー名
 * @property password パスワード
 * Used in: [com.example.myapp.controller.auth.login.LoginController]
 */

data class LoginRequest(
    val username: String,
    val password: String? = null, // 任意に変更
    val deviceId: String? = null
)
