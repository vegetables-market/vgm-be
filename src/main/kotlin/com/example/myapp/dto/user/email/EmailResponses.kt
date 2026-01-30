package com.example.myapp.dto.user.email

import java.time.LocalDateTime

data class EmailResponse(
    val emailId: Long,
    val email: String,
    val isPrimary: Boolean,
    val isVerified: Boolean,
    val createdAt: LocalDateTime
)
