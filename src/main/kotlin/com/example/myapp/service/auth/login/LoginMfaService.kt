package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.user.User
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.util.AuthUtils
import org.springframework.stereotype.Service

/**
 * ログインMFAサービス
 * ログイン時のMFA要求判定や、未確認デバイスへの対応を行う
 */
@Service
class LoginMfaService(
    private val mfaService: MfaService,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val emailVerificationService: EmailVerificationService,
    private val userEmailRepository: UserEmailRepository
) {

    /**
     * MFAチェックおよび未確認デバイスへの対応を行う
     *
     * @param user ユーザーエンティティ
     * @param isKnownDevice 既知のデバイスかどうか
     * @return MFAが必要な場合は MfaRunning (LoginResponseを含み、即座に返す用), 不要な場合は None
     */
    fun checkMfaStep(user: User, isKnownDevice: Boolean): MfaCheckResult {
        val authStatus = userAuthStatusRepository.findByUserId(user.userId)
        
        // 1. MFAが有効な場合
        if (authStatus != null && authStatus.isMfaEnabled) {
            val email = getPrimaryEmail(user.userId)
            val mfaToken = mfaService.generateLoginMfaToken(user.userId)
            
            if (authStatus.primaryMfaType == "TOTP") {
                return MfaCheckResult.MfaRunning(
                    LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfaToken = mfaToken,
                        mfaType = "TOTP",
                        maskedEmail = email?.let { AuthUtils.maskEmail(it) }
                    )
                )
            } else if (authStatus.primaryMfaType == "EMAIL") {
                if (email == null) throw RuntimeException("メールアドレスが登録されていません")
                emailVerificationService.sendVerificationEmail(user.userId, email)
                
                return MfaCheckResult.MfaRunning(
                    LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfaToken = mfaToken,
                        mfaType = "EMAIL",
                        maskedEmail = AuthUtils.maskEmail(email)
                    )
                )
            }
        }

        // 2. MFA無効だが、未知のデバイスの場合 -> メール認証を要求
        if (!isKnownDevice) {
            val email = getPrimaryEmail(user.userId) ?: user.username // usernameがemailの場合もあるが、基本はPrimaryEmail
            // そもそもEmailがない場合はどうする？ -> エラーになる可能性が高いが、ここでは既存ロジック踏襲
            
            val (flowId, _) = emailVerificationService.sendVerificationEmail(user.userId, email)
            val maskedEmail = getPrimaryEmail(user.userId)?.let { AuthUtils.maskEmail(it) }

            return MfaCheckResult.MfaRunning(
                LoginResponse(
                    status = "VERIFICATION_REQUIRED",
                    user = null,
                    requireVerification = true,
                    flowId = flowId,
                    maskedEmail = maskedEmail,
                    mfaToken = null
                )
            )
        }

        return MfaCheckResult.None
    }

    private fun getPrimaryEmail(userId: Int): String? {
        return userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)?.email
    }

    sealed class MfaCheckResult {
        data class MfaRunning(val response: LoginResponse) : MfaCheckResult()
        object None : MfaCheckResult()
    }
}
