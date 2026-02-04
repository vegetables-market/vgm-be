package com.example.myapp.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_emails")
data class UserEmail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_email_id")
    val emailId: Long = 0,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_email", nullable = false, unique = true, length = 255)
    val email: String,

    @Column(name = "f_type", nullable = false, length = 20)
    val type: String,  // 'PRIMARY', 'OAUTH', 'SUB'

    @Column(name = "f_source", length = 50)
    val source: String? = null,  // 'MANUAL', 'GOOGLE', 'APPLE'

    @Column(name = "f_is_verified")
    var isVerified: Boolean = false,

    @Column(name = "f_is_primary")
    var isPrimary: Boolean = false,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
