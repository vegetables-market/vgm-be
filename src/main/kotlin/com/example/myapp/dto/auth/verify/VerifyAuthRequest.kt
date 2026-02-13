package com.example.myapp.dto.auth.verify

/**
 * 認証/アクション検証リクエストDTO
 *
 * MFAコード送信や、重要なアクション前の再認証に使用される。
 *
 * @property method 認証方法 (EMAIL, TOTP)
 * @property identifier 識別子 (flowId or mfaToken)
 * @property code 認証コード (OTP)
 * @property action アクション (任意)
 * Used in: [com.example.myapp.controller.auth.verify.LoginVerificationController]
 */

enum class AuthMethod {
    EMAIL, TOTP, PASSWORD
}

data class VerifyAuthRequest(
    val method: AuthMethod,
    val identifier: String, // flow_id or mfa_token
    val code: String,
    val action: String? = null
)
