package com.example.myapp.dto.auth

data class LoginRequest(
    val username: String,
    val password: String? = null, // 任意に変更
    val device_id: String? = null
)
