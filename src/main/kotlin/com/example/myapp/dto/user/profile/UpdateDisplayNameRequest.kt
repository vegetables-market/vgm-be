package com.example.myapp.dto.user.profile

/**
 * 表示名更新リクエストDTO
 *
 * @property displayName 新しい表示名
 * Used in: [com.example.myapp.controller.user.account.UpdateDisplayNameController]
 */

data class UpdateDisplayNameRequest(
    val displayName: String,
    val password: String? = null
)
