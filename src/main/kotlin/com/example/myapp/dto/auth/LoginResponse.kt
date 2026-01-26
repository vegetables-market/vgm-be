package com.example.myapp.dto.auth

data class LoginResponse(
    val status: String,
    val user: UserInfo?,
    val require_verification: Boolean = false,
    val flow_id: String? = null,
    val masked_email: String? = null, // 追加: マスキングされたメールアドレス
    val requireTotp: Boolean = false
)

data class UserInfo(
    val user_id: Int,
    val display_name: String,
    val email: String?,
    val avatar_url: String?,
    val is_email_verified: Boolean
)
