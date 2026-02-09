package com.example.myapp.service.email.verification

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * メール認証コード検証ユースケース
 */
@Service
class VerifyEmailCode(
    private val verificationCodeRepository: VerificationCodeRepository,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {

    /**
     * flowId とコードによる検証
     */
    @Transactional
    fun verifyByFlowId(flowId: String, code: String): VerificationCode? {
        println("===== Verifying Code =====")
        println("Input FlowID: $flowId")
        println("Input Code: $code")
        
        // Typeを指定せずに検索 (EMAIL_VERIFY or ACTION_VERIFY)
        val verification = verificationCodeRepository.findByFlowIdAndCodeAndIsUsedFalseAndExpiresAtAfter(
            flowId = flowId,
            code = code,
            now = LocalDateTime.now()
        )
        
        if (verification == null) {
            println("Result: Verification failed (Not found or expired)")
            return null
        }

        // 許可されたタイプかチェック
        if (verification.type != "EMAIL_VERIFY" && verification.type != "ACTION_VERIFY") {
             println("Result: Verification failed (Invalid type: ${verification.type})")
             return null
        }

        println("Result: Verification success")
        verification.isUsed = true
        verificationCodeRepository.save(verification)

        // EMAIL_VERIFYの場合のみ、User/AuthStatusの更新を行う
        if (verification.type == "EMAIL_VERIFY" && verification.userId != null) {
            updateUserStatus(verification.userId!!)
        }

        return verification
    }

    /**
     * 認証コードの検証 (旧方式互換 - Identifier + Code)
     */
    @Transactional
    fun verifyByIdentifier(identifier: String, code: String): Boolean {
        // メールアドレスまたはユーザー名で検索
        val user = userRepository.findByUsername(identifier) 
            ?: userEmailRepository.findByEmail(identifier)?.let { 
                userRepository.findById(it.userId).orElse(null) 
            }
        val targetEmail = userEmailRepository.findByUserIdAndIsPrimaryTrue(user?.userId ?: 0)?.email ?: identifier

        val verification = verificationCodeRepository.findByEmailAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            email = targetEmail,
            code = code,
            type = "EMAIL_VERIFY",
            now = LocalDateTime.now()
        ) ?: return false

        verification.isUsed = true
        verificationCodeRepository.save(verification)

        if (user != null) {
            updateUserStatus(user.userId)
        }

        return true
    }

    /**
     * MFA用のコード検証（User IDベース）
     */
    @Transactional
    fun verifyForMfa(userId: Int, code: String): Boolean {
        // userIdで検索（typeはEMAIL_VERIFYを流用）
        val codes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "EMAIL_VERIFY")
        
        val now = LocalDateTime.now()
        val validCode = codes.find {
            it.code == code && it.expiresAt > now
        } ?: return false
        
        validCode.isUsed = true
        verificationCodeRepository.save(validCode)
        
        return true
    }

    private fun updateUserStatus(userId: Int) {
        // Update AuthStatus
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        if (authStatus != null) {
            authStatus.emailVerified = true
            userAuthStatusRepository.save(authStatus)
        }
        // Update User status
        userRepository.findById(userId).ifPresent { user ->
            user.status = 2  // Active
            userRepository.save(user)
        }
        // Update email verified status
        val emailRecord = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
        if (emailRecord != null) {
            emailRecord.isVerified = true
            userEmailRepository.save(emailRecord)
        }
    }
}
