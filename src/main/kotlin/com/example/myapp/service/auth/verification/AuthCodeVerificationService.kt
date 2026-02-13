package com.example.myapp.service.auth.verification

import com.example.myapp.dto.auth.verify.AuthMethod
import com.example.myapp.service.email.verification.VerifyEmailCode
import com.example.myapp.service.auth.MfaService
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.stereotype.Service

@Service
class AuthCodeVerificationService(
    private val verifyEmailCode: VerifyEmailCode,
    private val mfaService: MfaService,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val verificationCodeRepository: com.example.myapp.repository.auth.VerificationCodeRepository,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) {

    /**
     * 共通の認証コード検証ロジック
     * EMAIL/TOTP/PASSWORDの認証コードを検証してユーザーIDを返す
     */
    fun verifyAuthCode(method: AuthMethod, identifier: String, code: String): Int? {
        return when (method) {
            AuthMethod.EMAIL -> verifyEmailCode(identifier, code)
            AuthMethod.TOTP -> verifyTotpCode(identifier, code)
            AuthMethod.PASSWORD -> verifyPassword(identifier, code)
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

    /**
     * パスワード検証
     * identifier = flowId
     * code = password
     */
    private fun verifyPassword(flowId: String, password: String): Int? {
        // flowIdからユーザーを特定 (PASSWORD_FLOWタイプのVerificationCodeを使用)
        val verification = verificationCodeRepository.findByFlowIdAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId, "PASSWORD_FLOW", java.time.LocalDateTime.now()
        ) ?: return null

        val userId = verification.userId ?: return null
        val user = userRepository.findById(userId).orElse(null) ?: return null

        if (passwordEncoder.matches(password, user.passwordHash)) {
            // パスワード機能の場合はVerificationCodeを消費扱いにしない（再利用はしないが、アクション完了までflowIdは必要かも？）
            // いや、verifyAuthCodeはあくまで「本人確認」なので、ここで通ればOK。
            // ただしVerificationCode自体はActionVerificationControllerで「issueActionToken」が呼ばれるまで有効である必要がある？
            // ActionVerificationControllerは verifyAuthCode -> issueActionToken の順で呼ぶ。
            // issueActionTokenは新たにActionTokenを発行するので、このVerificationCodeはここで消費してしまっても良い？
            // 既存のEmailフローでは verifyEmailCode 内で isUsed=true にしていることが多い。
            // ここでも消費しておくのが安全。
            verification.isUsed = true
            verificationCodeRepository.save(verification)
            return userId
        }
        return null
    }
}
