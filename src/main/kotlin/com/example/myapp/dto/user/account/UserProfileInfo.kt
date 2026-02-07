package com.example.myapp.dto.user.account

data class UserProfileInfo(
    val userId: Int,
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String?,
    val hasPassword: Boolean,
    val role: String,
    val isEmailVerified: Boolean
)
