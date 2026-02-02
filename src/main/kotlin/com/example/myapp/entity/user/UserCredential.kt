package com.example.myapp.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "m_user_credentials")
data class UserCredential(
    @Id
    @Column(name = "f_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    // WebAuthn Credential ID (Base64Url encoded)
    @Column(name = "f_credential_id", nullable = false, unique = true, columnDefinition = "TEXT")
    val credentialId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "f_user_id", nullable = false)
    val user: User,

    @Column(name = "f_credential_name", nullable = false)
    var name: String,

    // COSE Key (Base64Url encoded)
    @Column(name = "f_public_key", nullable = false, columnDefinition = "TEXT")
    val publicKey: String,

    @Column(name = "f_sign_count", nullable = false)
    var signCount: Long = 0,

    // Comma separated transports (usb, nfc, ble, internal)
    @Column(name = "f_transports")
    var transports: String? = null,

    @Column(name = "f_created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "f_last_used_at")
    var lastUsedAt: LocalDateTime? = null
)
