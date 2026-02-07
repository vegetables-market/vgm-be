package com.example.myapp.dto.auth.login

data class LoginResponse(
    val status: String,
    val user: UserInfo?,
    val message: String? = null,
    val requireVerification: Boolean = false,
    val flowId: String? = null,
    val maskedEmail: String? = null,
    val mfaToken: String? = null, // MFA検証用の一時トークン
    val mfaType: String? = null // "TOTP", "EMAIL" etc.
)

data class UserInfo(
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String? = null,
    val isEmailVerified: Boolean
)
