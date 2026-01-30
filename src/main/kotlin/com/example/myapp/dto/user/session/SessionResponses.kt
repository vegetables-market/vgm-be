package com.example.myapp.dto.user.session

import java.time.LocalDateTime

data class SessionResponse(
    val sessionId: Long,
    val deviceInfo: String?,
    val ipAddress: String?,
    val createdAt: LocalDateTime,
    val lastActiveAt: LocalDateTime?,
    val expiresAt: LocalDateTime,
    val isCurrent: Boolean
)
