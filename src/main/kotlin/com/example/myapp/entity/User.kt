package com.example.myapp.entity

import jakarta.persistence.*

@Entity
@Table(name = "m_users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_user_id")
    val id: Int = 0,

    @Column(name = "f_username", nullable = false, unique = true)
    val username: String = "",

    @Column(name = "f_password_hash", nullable = false)
    val password: String = "",

    // @Column(name = "f_email")
    // val email: String = "",

    @Column(name = "f_created_at")
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    @Column(name = "f_totp_secret", length = 32)
    var totpSecret: String? = null,

    @Column(name = "f_two_factor_verified")
    var totpEnabled: Boolean = false
)
