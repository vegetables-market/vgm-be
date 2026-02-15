package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_recovery_sessions")
data class RecoverySession(
    @Id
    @Column(name = "f_session_id", length = 36)
    val sessionId: String, // UUID

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_status", nullable = false, length = 20)
    var status: String, // CREATED, CHALLENGE_SENT, VERIFIED, COMPLETED, LOCKED, EXPIRED

    @Column(name = "f_expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "f_attempt_count")
    var attemptCount: Int = 0,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
