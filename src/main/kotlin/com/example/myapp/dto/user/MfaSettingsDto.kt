package com.example.myapp.dto.user

data class MfaSetupResponse(
    val secret: String,
    val qrCodeUrl: String
)

data class MfaVerifyRequest(
    val code: String
)

data class MfaEnableResponse(
    val success: Boolean,
    val backupCodes: List<String>
)

data class MfaDisableRequest(
    val code: String,
    val password: String
)

data class RegenerateCodesRequest(
    val password: String
)

data class BackupCodesResponse(
    val backupCodes: List<String>
)

data class MfaStatusResponse(
    val isEnabled: Boolean,
    val createdAt: String?
)
