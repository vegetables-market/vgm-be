package com.example.myapp.dto.auth

data class VerifyEmailRequest(
    val identifier: String, // email または username
    val code: String
)
