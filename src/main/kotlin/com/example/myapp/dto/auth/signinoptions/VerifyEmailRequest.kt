package com.example.myapp.dto.auth.signinoptions

data class VerifyEmailRequest(
    val identifier: String, // email または username
    val code: String
)
