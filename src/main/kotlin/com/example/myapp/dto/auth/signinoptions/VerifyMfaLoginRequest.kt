package com.example.myapp.dto.auth.signinoptions

data class VerifyMfaLoginRequest(
    val mfa_token: String,
    val code: String
)
