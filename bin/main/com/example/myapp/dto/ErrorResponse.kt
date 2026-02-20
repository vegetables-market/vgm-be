package com.example.myapp.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val errorCode: String,
    val message: String,
    val details: List<String> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)
