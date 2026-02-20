package com.example.myapp.dto.user.profile

data class ChangePasswordRequest(
    val currentPassword: String?,
    val newPassword: String
)

data class ChangeUsernameRequest(
    val newUsername: String,
    val password: String?
)
