package com.example.myapp.dto.auth.flow

/**
 * 認証コード再送リクエストDTO
 *
 * @property flowId 認証フローID
 * Used in: [com.example.myapp.controller.auth.flow.ResendCodeController]
 */

data class ResendCodeRequest(
    val flowId: String
)
