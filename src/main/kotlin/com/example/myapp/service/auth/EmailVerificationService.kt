package com.example.myapp.service.auth

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.service.common.EmailService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmailVerificationService(
    private val verificationCodeRepository: VerificationCodeRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {

    /**
     * 認証コードを生成してメール送信
     * @return 生成された flowId
     */
    @Transactional
    fun sendVerificationEmail(userId: Int, email: String): String {
        // 既存の未使用コードを無効化
        val existingCodes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "EMAIL_VERIFY")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        val code = (100000..999999).random().toString()
        val flowId = UUID.randomUUID().toString()
        
        val verificationCode = VerificationCode(
            userId = userId,
            email = email,
            code = code,
            flowId = flowId,
            type = "EMAIL_VERIFY",
            expiresAt = LocalDateTime.now().plusMinutes(20)
        )
        
        verificationCodeRepository.save(verificationCode)
        
        // 開発用ログ
        println("===== Verification Code Generated =====")
        println("FlowID: $flowId")
        println("Code: $code")
        println("Email: $email")
        println("=======================================")
        
        emailService.sendVerificationCodeEmail(email, code)
        return flowId
    }

    /**
     * flowId に基づいてコードを再送信
     * @return 新しい flowId
     */
    @Transactional
    fun resendVerificationEmail(oldFlowId: String): String? {
        // 古いflowIdからユーザー情報を特定
        val oldRecord = verificationCodeRepository.findByFlowId(oldFlowId) ?: return null
        
        if (oldRecord.userId == null || oldRecord.email == null) return null
        
        // 新しいコードを発行（内部で古いコードは無効化される）
        return sendVerificationEmail(oldRecord.userId, oldRecord.email)
    }

    /**
     * flowId とコードによる検証
     */
    @Transactional
    fun verifyByFlowId(flowId: String, code: String): VerificationCode? {
        println("===== Verifying Code =====")
        println("Input FlowID: $flowId")
        println("Input Code: $code")
        
        val verification = verificationCodeRepository.findByFlowIdAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId = flowId,
            code = code,
            type = "EMAIL_VERIFY",
            now = LocalDateTime.now()
        )
        
        if (verification == null) {
            println("Result: Verification failed (Not found or expired)")
            return null
        }

        println("Result: Verification success")
        verification.isUsed = true
        verificationCodeRepository.save(verification)

        // ユーザーのステータス更新
        if (verification.userId != null) {
            userRepository.findById(verification.userId).ifPresent { user ->
                user.emailVerified = 1
                user.status = 2
                userRepository.save(user)
            }
        }

        return verification
    }

    /**
     * 認証コードの検証 (旧方式互換)
     */
    @Transactional
    fun verifyEmail(identifier: String, code: String): Boolean {
        val user = userRepository.findByUsernameOrEmail(identifier, identifier)
        val targetEmail = user?.email ?: identifier

        val verification = verificationCodeRepository.findByEmailAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            email = targetEmail,
            code = code,
            type = "EMAIL_VERIFY",
            now = LocalDateTime.now()
        ) ?: return false

        verification.isUsed = true
        verificationCodeRepository.save(verification)

        if (user != null) {
            user.emailVerified = 1
            user.status = 2
            userRepository.save(user)
        }

        return true
    }
    /**
     * MFA用のコード検証（User IDベース）
     */
    @Transactional
    fun verifyCodeForMfa(userId: Int, code: String): Boolean {
        // userIdで検索（typeはEMAIL_VERIFYを流用、またはEMAIL_MFAと分ける手もあるが、今回は流用）
        val codes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "EMAIL_VERIFY")
        
        // 有効期限内かつコードが一致するものを探す
        val now = LocalDateTime.now()
        val validCode = codes.find {
            it.code == code && it.expiresAt > now
        } ?: return false
        
        validCode.isUsed = true
        verificationCodeRepository.save(validCode)
        
        return true
    }
}
