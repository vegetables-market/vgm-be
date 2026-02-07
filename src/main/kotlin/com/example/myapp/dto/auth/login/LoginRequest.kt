package com.example.myapp.dto.auth.login

data class LoginRequest(
    val username: String,
    val password: String? = null, // 任意に変更
    val deviceId: String? = null
)
