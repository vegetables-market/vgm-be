package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_verification_codes")
data class VerificationCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_code_id")
    val codeId: Long = 0,

    @Column(name = "f_user_id")
    val userId: Int? = null,

    @Column(name = "f_email")
    val email: String? = null,

    @Column(name = "f_code", nullable = false, length = 50)
    var code: String,

    @Column(name = "f_flow_id", unique = true) // 追加
    val flowId: String? = null,

    @Column(name = "f_type", nullable = false, length = 50)
    val type: String, // "EMAIL_VERIFY", "PASSWORD_RESET", "2FA_SMS"

    @Column(name = "f_expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(name = "f_is_used")
    var isUsed: Boolean = false,

    @Column(name = "f_resend_count")
    var resendCount: Int = 0,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
