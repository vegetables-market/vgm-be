package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_auth_status")
data class UserAuthStatus(
    @Id
    @Column(name = "f_user_id")
    val userId: Int,

    @Column(name = "f_email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "f_phone_verified")
    var phoneVerified: Boolean = false,

    @Column(name = "f_identity_verified")
    var identityVerified: Boolean = false,

    @Column(name = "f_is_mfa_enabled")
    var isMfaEnabled: Boolean = false,

    @Column(name = "f_primary_mfa_type", length = 20)
    var primaryMfaType: String? = null,

    @Column(name = "f_failed_attempts")
    var failedAttempts: Int = 0,

    @Column(name = "f_locked_until")
    var lockedUntil: LocalDateTime? = null,

    @Column(name = "f_last_failed_at")
    var lastFailedAt: LocalDateTime? = null,

    @Column(name = "f_has_password")
    var hasPassword: Boolean = false,

    @Column(name = "f_last_auth_method", length = 20)
    var lastAuthMethod: String? = null,

    @Column(name = "f_last_auth_at")
    var lastAuthAt: LocalDateTime? = null,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
