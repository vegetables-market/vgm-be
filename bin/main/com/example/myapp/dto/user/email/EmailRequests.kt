package com.example.myapp.dto.user.email

data class AddEmailRequest(
    val email: String
)

data class VerifyEmailRequest(
    val flowId: String,
    val code: String
)
