package com.example.myapp.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user_devices")
data class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_device_id")
    val id: Long = 0,

    @Column(name = "f_user_id", nullable = false)
    val userId: Int,

    @Column(name = "f_fcm_token", nullable = false, unique = true, length = 512)
    val fcmToken: String,

    @Column(name = "f_device_type", length = 20)
    val deviceType: String? = null, // ANDROID, IOS, WEB

    @Column(name = "f_app_version", length = 50)
    val appVersion: String? = null,

    @Column(name = "f_os_version", length = 50)
    val osVersion: String? = null,

    @Column(name = "f_last_active_at")
    var lastActiveAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
