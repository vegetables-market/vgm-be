package com.example.myapp.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "m_users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_user_id")
    val userId: Int = 0,

    @Column(name = "f_username", nullable = false, unique = true, length = 100)
    var username: String,

    @Column(name = "f_display_name", nullable = false, length = 100)
    var displayName: String,

    @Column(name = "f_password_hash", length = 255)
    var passwordHash: String? = null,  // NULL許容（OAuth専用ユーザー対応）

    @Column(name = "f_last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @Column(name = "f_status")
    var status: Short = 1, // 0:無効, 1:仮登録, 2:有効, 3:停止, 4:削除

    @Column(name = "f_role", nullable = false, length = 20)
    var role: String = "USER",

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
