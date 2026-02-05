package com.example.myapp.dto.auth.signinoptions

data class VerifyMfaLoginRequest(
    val mfa_token: String,
    val code: String,
    val action: String? = null // Optional: アクションタイプ (例: "delete_account")
)
