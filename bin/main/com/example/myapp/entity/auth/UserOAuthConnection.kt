package com.example.myapp.entity.auth

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_oauth_connections")
data class UserOAuthConnection(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_connection_id")
    val connectionId: Long = 0,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_provider", nullable = false, length = 50)
    val provider: String,  // 'google', 'apple', 'line'

    @Column(name = "f_provider_user_id", nullable = false, length = 255)
    val providerUserId: String,  // Firebase UID or provider's sub

    @Column(name = "f_email_id")
    val emailId: Long? = null,

    @Column(name = "f_display_name", length = 255)
    val displayName: String? = null,

    @Column(name = "f_avatar_url", length = 500)
    val avatarUrl: String? = null,

    @Column(name = "f_linked_at")
    val linkedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_last_used_at")
    var lastUsedAt: LocalDateTime? = null,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
