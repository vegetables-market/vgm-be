package com.example.myapp.service.email.verification

import com.example.myapp.repository.auth.VerificationCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 認証コード再送信ユースケース
 */
@Service
class ResendVerificationEmail(
    private val verificationCodeRepository: VerificationCodeRepository,
    private val sendVerificationEmail: SendVerificationEmail
) {

    /**
     * flowId に基づいてコードを再送信
     * @return 新しい flowId, expiresAt, createdAt
     */
    @Transactional
    operator fun invoke(oldFlowId: String): Triple<String, LocalDateTime, LocalDateTime>? {
        // 古いflowIdからユーザー情報を特定
        val oldRecord = verificationCodeRepository.findByFlowId(oldFlowId) ?: return null
        
        // 再送信回数チェック (最大3回まで再送信可能 = 計4回送信)
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
             sendVerificationEmail(oldRecord.userId!!, email, nextResendCount)
        } else {
             sendVerificationEmail.sendPreRegistration(email, nextResendCount)
        }
    }
}
