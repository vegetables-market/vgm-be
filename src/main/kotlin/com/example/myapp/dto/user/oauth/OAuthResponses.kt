package com.example.myapp.dto.user.oauth

import java.time.LocalDateTime

data class OAuthConnectionResponse(
    val connectionId: Long,
    val provider: String,
    val providerEmail: String?,
    val connectedAt: LocalDateTime
)
