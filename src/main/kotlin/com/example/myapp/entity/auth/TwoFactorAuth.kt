package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_two_factor")
data class TwoFactorAuth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_two_factor_id")
    val twoFactorId: Long = 0,

    @Column(name = "f_user_id", nullable = false, unique = true)
    val userId: Int,

    @Column(name = "f_secret_key", nullable = false, length = 255)
    var secretKey: String,

    @Column(name = "f_backup_codes", columnDefinition = "TEXT")
    var backupCodes: String? = null,

    @Column(name = "f_is_enabled", nullable = false)
    var isEnabled: Boolean = false,

    @Column(name = "f_created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
