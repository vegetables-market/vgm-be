package com.example.myapp.dto.auth

data class InitauthRequest(
    val email: String
)

data class VerifyCodeRequest(
    val flow_id: String,
    val code: String
)

data class ResendCodeRequest(
    val flow_id: String
)
