package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.user.User
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.email.verification.SendVerificationEmail
import com.example.myapp.util.AuthUtils
import org.springframework.stereotype.Service

/**
 * ログインMFAチェックユースケース
 * ログイン時のMFA要求判定や、未確認デバイスへの対応を行う
 */
@Service
class CheckLoginMfa(
    private val mfaService: MfaService,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val sendVerificationEmail: SendVerificationEmail,
    private val userEmailRepository: UserEmailRepository
) {

    sealed class Result {
        data class MfaRunning(val response: LoginResponse) : Result()
        object None : Result()
    }

    /**
     * MFAチェックおよび未確認デバイスへの対応を行う
     */
    operator fun invoke(user: User, isKnownDevice: Boolean): Result {
        val authStatus = userAuthStatusRepository.findByUserId(user.userId)
        
        // 1. MFAが有効な場合
        if (authStatus != null && authStatus.isMfaEnabled) {
            val email = getPrimaryEmail(user.userId)
            val mfaToken = mfaService.generateLoginMfaToken(user.userId)
            
            if (authStatus.primaryMfaType == "TOTP") {
                return Result.MfaRunning(
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
                val (emailFlowId, expiresAt, _) = sendVerificationEmail(user.userId, email)
                
                return Result.MfaRunning(
                    LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfaToken = mfaToken,
                        mfaType = "EMAIL",
                        maskedEmail = AuthUtils.maskEmail(email),
                        flowId = emailFlowId,
                        expiresAt = expiresAt.toString()
                    )
                )
            }
        }

        // 2. MFA無効だが、未知のデバイスの場合 -> メール認証を要求
        if (!isKnownDevice) {
            val email = getPrimaryEmail(user.userId) ?: user.username
            
            val (flowId, expiresAt, _) = sendVerificationEmail(user.userId, email)
            val maskedEmail = getPrimaryEmail(user.userId)?.let { AuthUtils.maskEmail(it) }

            return Result.MfaRunning(
                LoginResponse(
                    status = "VERIFICATION_REQUIRED",
                    user = null,
                    requireVerification = true,
                    flowId = flowId,
                    maskedEmail = maskedEmail,
                    mfaToken = null,
                    expiresAt = expiresAt.toString()
                )
            )
        }

        return Result.None
    }

    private fun getPrimaryEmail(userId: Int): String? {
        return userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)?.email
    }
}
