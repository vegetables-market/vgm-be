package com.example.myapp.service.email

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmailVerificationService(
    private val verificationCodeRepository: VerificationCodeRepository,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val emailNotificationService: EmailNotificationService
) {

    /**
     * 認証コードを生成してメール送信
     * @return flowId と expiresAt のペア
     */
    @Transactional
    fun sendVerificationEmail(userId: Int, email: String, resendCount: Int = 0): Triple<String, LocalDateTime, LocalDateTime> {
        // 既存の未使用コードを無効化
        val existingCodes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "EMAIL_VERIFY")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        val code = (100000..999999).random().toString()
        val flowId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val expiresAt = now.plusMinutes(10)
        
        val verificationCode = VerificationCode(
            userId = userId,
            email = email,
            code = code,
            flowId = flowId,
            type = "EMAIL_VERIFY",
            expiresAt = expiresAt,
            resendCount = resendCount
        )
        
        verificationCodeRepository.save(verificationCode)
        
        // 開発用ログ
        println("===== Verification Code Generated =====")
        println("FlowID: $flowId")
        println("Code: $code")
        println("Email: $email")
        println("=======================================")
        
        emailNotificationService.sendVerificationCodeEmail(email, code)
        return Triple(flowId, expiresAt, now)
    }

    /**
     * 未登録ユーザー向けの認証コード送信
     */
    @Transactional
    fun sendPreRegistrationVerificationEmail(email: String, resendCount: Int = 0): Triple<String, LocalDateTime, LocalDateTime> {
        // 既存の未使用コードを無効化 (Emailベース)
        val existingCodes = verificationCodeRepository.findByEmailAndTypeAndIsUsedFalse(email, "EMAIL_VERIFY")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        val code = (100000..999999).random().toString()
        val flowId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val expiresAt = now.plusMinutes(10)

        val verificationCode = VerificationCode(
            userId = null, // 未登録ユーザー
            email = email,
            code = code,
            flowId = flowId,
            type = "EMAIL_VERIFY",
            expiresAt = expiresAt,
            resendCount = resendCount
        )

        verificationCodeRepository.save(verificationCode)
        
        // 開発用ログ
        println("===== Pre-Registration Code Generated =====")
        println("FlowID: $flowId")
        println("Code: $code")
        println("Email: $email")
        println("=======================================")

        emailNotificationService.sendVerificationCodeEmail(email, code)
        return Triple(flowId, expiresAt, now)
    }

    /**
     * flowId に基づいてコードを再送信
     * @return 新しい flowId と expiresAt
     */
    /**
     * flowId に基づいてコードを再送信
     * @return 新しい flowId, expiresAt, createdAt
     */
    @Transactional
    fun resendVerificationEmail(oldFlowId: String): Triple<String, LocalDateTime, LocalDateTime>? {
        // 古いflowIdからユーザー情報を特定
        val oldRecord = verificationCodeRepository.findByFlowId(oldFlowId) ?: return null
        
        // 再送信回数チェック (最大3回まで再送信可能 = 計4回送信)
        // ユーザー要望: "3回再送信すると、その認証はできなくなる" -> resendCountが3に達したらNG
        if (oldRecord.resendCount >= 3) {
            throw RuntimeException("RESEND_LIMIT_EXCEEDED")
        }

        // レート制限チェック (30秒)
        val now = LocalDateTime.now()
        if (oldRecord.createdAt.plusSeconds(30).isAfter(now)) {
            throw RuntimeException("Please wait at least 30 seconds before resending.")
        }

        val email = oldRecord.email ?: return null
        val nextResendCount = oldRecord.resendCount + 1

        // 新しいコードを発行（内部で古いコードは無効化される）
        return if (oldRecord.userId != null) {
             sendVerificationEmail(oldRecord.userId, email, nextResendCount)
        } else {
             sendPreRegistrationVerificationEmail(email, nextResendCount)
        }
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

        // UserAuthStatus と User のステータス更新
        if (verification.userId != null) {
            // Update AuthStatus
            val authStatus = userAuthStatusRepository.findByUserId(verification.userId)
            if (authStatus != null) {
                authStatus.emailVerified = true
                userAuthStatusRepository.save(authStatus)
            }
            // Update User status
            userRepository.findById(verification.userId).ifPresent { user ->
                user.status = 2  // Active
                userRepository.save(user)
            }
            // Update email verified status
            val emailRecord = userEmailRepository.findByUserIdAndIsPrimaryTrue(verification.userId)
            if (emailRecord != null) {
                emailRecord.isVerified = true
                userEmailRepository.save(emailRecord)
            }
        }

        return verification
    }

    /**
     * 認証コードの検証 (旧方式互換)
     */
    @Transactional
    fun verifyEmail(identifier: String, code: String): Boolean {
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
            // Update AuthStatus
            val authStatus = userAuthStatusRepository.findByUserId(user.userId)
            if (authStatus != null) {
                authStatus.emailVerified = true
                userAuthStatusRepository.save(authStatus)
            }
            // Update User status
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

    /**
     * flowId がメール認証済みかどうかを確認 (ユーザー登録時用)
     */
    @Transactional(readOnly = true)
    fun isFlowVerified(flowId: String, email: String): Boolean {
        val verification = verificationCodeRepository.findByFlowId(flowId) ?: return false
        
        // メールアドレスが一致し、使用済み(認証成功済み)であり、タイプがEMAIL_VERIFYであること
        return verification.email == email && 
               verification.isUsed && 
               verification.type == "EMAIL_VERIFY"
    }
}
