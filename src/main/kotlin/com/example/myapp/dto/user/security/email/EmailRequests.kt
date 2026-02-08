package com.example.myapp.dto.user.security.email

/**
 * メールアドレス追加リクエストDTO
 *
 * @property email 追加するメールアドレス
 * Used in: [com.example.myapp.controller.user.security.email.AddEmailController]
 */
data class AddEmailRequest(
    val email: String
)

/**
 * メールアドレス確認リクエストDTO
 *
 * @property flowId 確認フローID
 * @property code 確認コード
 * Used in: [com.example.myapp.controller.user.security.email.VerifyEmailController]
 */
data class VerifyEmailRequest(
    val flowId: String,
    val code: String
)
