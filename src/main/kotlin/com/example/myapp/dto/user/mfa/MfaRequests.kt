package com.example.myapp.dto.user.mfa

data class MfaVerifyRequest(
    val code: String
)

data class MfaDisableRequest(
    val code: String,
    val password: String
)

data class RegenerateCodesRequest(
    val password: String
)
