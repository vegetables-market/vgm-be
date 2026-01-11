package com.example.myapp.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String = "",

    @Column(nullable = false)
    val password: String = "",

    @Column(nullable = false)
    val email: String = "",

    @Column(name = "created_at")
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    @Column(name = "totp_secret", length = 32)
    var totpSecret: String? = null,

    @Column(name = "totp_enabled")
    var totpEnabled: Boolean = false
)
