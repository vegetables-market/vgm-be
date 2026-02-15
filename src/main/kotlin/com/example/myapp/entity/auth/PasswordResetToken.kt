package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_password_reset_tokens")
data class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_token_id")
    val tokenId: Long = 0,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_token_hash", nullable = false, length = 64)
    val tokenHash: String, // SHA-256 hash of the token

    @Column(name = "f_expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "f_is_used")
    var isUsed: Boolean = false,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
