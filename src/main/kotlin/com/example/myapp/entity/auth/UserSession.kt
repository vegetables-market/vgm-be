package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_sessions")
data class UserSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_session_id")
    val sessionId: Long = 0,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_session_key", nullable = false, unique = true)
    val sessionKey: String,

    @Column(name = "f_refresh_token_hash", nullable = false)
    val refreshTokenHash: String,

    @Column(name = "f_device_name")
    val deviceName: String? = null,

    @Column(name = "f_ip_address")
    val ipAddress: String? = null,

    @Column(name = "f_provider")
    val provider: String? = null,

    @Column(name = "f_is_revoked")
    var isRevoked: Boolean = false,

    @Column(name = "f_expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "f_last_accessed_at")
    var lastAccessedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
