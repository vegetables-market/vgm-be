package com.example.myapp.dto.auth.flow

/**
 * 認証フロー開始リクエストDTO
 *
 * @property email メールアドレス
 * Used in: [com.example.myapp.controller.auth.flow.InitFlowController]
 */

data class InitAuthRequest(
    val email: String
)
