package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_guest_sessions")
data class GuestSession(
    @Id
    @Column(name = "f_guest_id", length = 36)
    val guestId: String,

    @Column(name = "f_expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "f_created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
