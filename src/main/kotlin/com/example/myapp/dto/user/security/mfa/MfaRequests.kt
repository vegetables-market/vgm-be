package com.example.myapp.dto.user.security.mfa

/**
 * MFAコード検証リクエストDTO
 *
 * @property code MFAコード (TOTP)
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaVerifyController]
 */
data class MfaVerifyRequest(
    val code: String
)

/**
 * MFA無効化リクエストDTO
 *
 * @property code MFAコード
 * @property password パスワード (本人確認用)
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaDisableController]
 */
data class MfaDisableRequest(
    val code: String,
    val password: String
)

/**
 * バックアップコード再生成リクエストDTO
 *
 * @property password パスワード (本人確認用)
 * @property password パスワード (本人確認用)
 * Used in: [com.example.myapp.controller.user.security.mfa.MfaBackupCodeController]
 */
data class RegenerateCodesRequest(
    val password: String
)
