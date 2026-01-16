package com.example.myapp.dto

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null,
    val username: String? = null
)
