package com.example.myapp.dto.auth.flow

/**
 * 認証コード検証リクエストDTO
 *
 * @property flowId 認証フローID
 * @property code 入力された認証コード
 * Used in: [com.example.myapp.controller.auth.verify.CodeVerificationController]
 */

data class VerifyCodeRequest(
    val flowId: String,
    val code: String
)
