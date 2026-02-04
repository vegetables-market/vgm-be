package com.example.myapp.dto.user.profile

data class UpdateDisplayNameRequest(
    val displayName: String,
    val password: String? = null
)
