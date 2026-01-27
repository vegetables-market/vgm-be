package com.example.myapp.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(
    val status: String,
    val user: UserInfo?,
    val message: String? = null,
    val require_verification: Boolean = false,
    val flow_id: String? = null,
    val masked_email: String? = null,
    val mfa_token: String? = null // MFA検証用の一時トークン
)

data class UserInfo(
    val user_id: Int,
    val display_name: String,
    val email: String?,
    val avatar_url: String? = null,
    @JsonProperty("is_email_verified")
    val is_email_verified: Any // Boolean or Short
)
