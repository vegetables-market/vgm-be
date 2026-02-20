package com.example.myapp.dto

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Long? = null,
    val username: String? = null,
    val theme: String? = null
)
