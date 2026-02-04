package com.example.myapp.dto.user.mfa

data class MfaSetupResponse(
    val secret: String,
    val qrCodeUrl: String
)

data class MfaEnableResponse(
    val success: Boolean,
    val backupCodes: List<String>
)

data class BackupCodesResponse(
    val backupCodes: List<String>
)

data class MfaStatusResponse(
    val isEnabled: Boolean,
    val createdAt: String?
)
