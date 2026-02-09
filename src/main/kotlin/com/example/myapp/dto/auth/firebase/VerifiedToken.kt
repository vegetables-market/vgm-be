package com.example.myapp.dto.auth.firebase

/**
 * 検証済みトークン情報
 */
data class VerifiedToken(
    val uid: String,
    val email: String,
    val name: String,
    val picture: String?
)