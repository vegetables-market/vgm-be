package com.example.myapp.service.auth.verification

import com.example.myapp.dto.auth.verify.AuthMethod
import com.example.myapp.service.email.verification.VerifyEmailCode
import com.example.myapp.service.auth.MfaService
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.stereotype.Service

@Service
class AuthCodeVerificationService(
    private val verifyEmailCode: VerifyEmailCode,
    private val mfaService: MfaService,
    private val userEmailRepository: UserEmailRepository
) {

    /**
     * 共通の認証コード検証ロジック
     * EMAIL/TOTPの認証コードを検証してユーザーIDを返す
     */
    fun verifyAuthCode(method: AuthMethod, identifier: String, code: String): Int? {
        return when (method) {
            AuthMethod.EMAIL -> verifyEmailCode(identifier, code)
            AuthMethod.TOTP -> verifyTotpCode(identifier, code)
        }
    }

    /**
     * Email認証コードの検証
     */
    private fun verifyEmailCode(flowId: String, code: String): Int? {
        val verification = verifyEmailCode.verifyByFlowId(flowId, code) ?: return null
        return verification.userId ?: userEmailRepository.findByEmail(verification.email!!)?.userId
    }

    /**
     * TOTP認証コードの検証
     */
    private fun verifyTotpCode(mfaToken: String, code: String): Int? {
        return try {
            mfaService.verifyLoginMfa(mfaToken, code)
        } catch (e: Exception) {
            null
        }
    }
}
