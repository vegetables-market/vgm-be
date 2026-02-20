package com.example.myapp.entity.common

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "m_app_versions")
data class AppVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_version_id")
    val versionId: Long = 0,

    @Column(name = "f_platform")
    val platform: String = "pwa",

    @Column(name = "f_version_name")
    val versionName: String,

    @Column(name = "f_version_code")
    val versionCode: Int,

    @Column(name = "f_min_supported_version")
    val minSupportedVersion: Int = 0,

    @Column(name = "f_released_at")
    val releasedAt: LocalDateTime? = null,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "f_updated_at")
    val updatedAt: LocalDateTime? = null
)