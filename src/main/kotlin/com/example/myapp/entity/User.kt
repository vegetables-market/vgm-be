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
    var totpEnabled: Boolean = false,

    /**
     * User preference: theme.
     * DB: stored in table `users`, column name `theme` (TINYINT).
     * 0 = light, 1 = dark
     */
<<<<<<< HEAD
    @Column(name = "theme", columnDefinition = "TINYINT(1)")
    var theme: Byte = 0
=======
    @Column(name = "theme")
<<<<<<< HEAD
    var theme: Int = 0  // 0: light, 1: dark
=======
    var theme: String? = "light"
>>>>>>> 98a3824ee00446e97cfcc4f58bfe5960549fbdcb
>>>>>>> 4f7cb93640299e817facd2e4d1a78f462dc09144
)
