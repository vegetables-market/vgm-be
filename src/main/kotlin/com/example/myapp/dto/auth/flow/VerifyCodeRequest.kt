package com.example.myapp.dto.auth.flow

data class VerifyCodeRequest(
    val flowId: String,
    val code: String
)
