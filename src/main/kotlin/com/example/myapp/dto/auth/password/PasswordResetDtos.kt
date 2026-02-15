package com.example.myapp.dto.auth.password

data class PasswordResetRequest(
    val token: String,
    val newPassword: String
)
