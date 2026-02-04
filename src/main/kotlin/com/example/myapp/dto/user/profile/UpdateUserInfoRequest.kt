package com.example.myapp.dto.user.profile

data class UpdateUserInfoRequest(
    val gender: Short? = null,
    val birthDate: String? = null  // YYYY-MM-DD format
)
