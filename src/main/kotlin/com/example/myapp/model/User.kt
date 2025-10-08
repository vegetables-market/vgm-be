package com.example.myapp.model

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String,
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)