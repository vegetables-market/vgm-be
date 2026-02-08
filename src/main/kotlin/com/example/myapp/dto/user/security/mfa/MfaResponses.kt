package com.example.myapp.dto.user.security.mfa

/**
 * MFAセットアップ開始レスポンスDTO
 *
 * @property secret TOTPシークレット（Base32）
 * @property qrCodeUrl QRコード生成用URL
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaStartController]
 */
data class MfaSetupResponse(
    val secret: String,
    val qrCodeUrl: String
)

/**
 * MFA有効化完了レスポンスDTO
 *
 * @property success 成功フラグ
 * @property backupCodes バックアップコードリスト
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaVerifyController]
 */
data class MfaEnableResponse(
    val success: Boolean,
    val backupCodes: List<String>
)

/**
 * バックアップコード応答DTO
 *
 * @property backupCodes バックアップコードリスト
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaBackupCodeController]
 */
data class BackupCodesResponse(
    val backupCodes: List<String>
)

/**
 * MFAステータスレスポンスDTO
 *
 * @property isEnabled 有効かどうか
 * @property createdAt 有効化日時
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaStatusController]
 */
data class MfaStatusResponse(
    val isEnabled: Boolean,
    val createdAt: String?
)
