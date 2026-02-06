package com.example.myapp.dto.auth

enum class AuthMethod {
    EMAIL, TOTP
}

data class VerifyAuthRequest(
    val method: AuthMethod,
    val identifier: String, // flow_id or mfa_token
    val code: String,
    val action: String? = null
)
