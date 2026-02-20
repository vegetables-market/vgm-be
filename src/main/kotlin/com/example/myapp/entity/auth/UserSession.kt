package com.example.myapp.entity.auth

/**
 * 簡易なユーザーセッション DTO。
 * 実行時には認証フィルターなどで principal にセットされる想定です。
 */
data class UserSession(
    val userId: Long,
    val username: String? = null,
    val theme: String? = null
)
