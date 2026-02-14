package com.example.myapp.service.email.verification

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.VerificationCodeEmailTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * 認証コード送信ユースケース
 * 認証コードを生成・保存し、メールで送信する
 */
@Service
class SendVerificationEmail(
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailSenderService: EmailSenderService,
    private val verificationCodeEmailTemplate: VerificationCodeEmailTemplate
) {

    /**
     * 登録済みユーザー向けの認証コード送信
     * @return flowId と expiresAt のペア
     */
    @Transactional
    operator fun invoke(userId: Int, email: String, resendCount: Int = 0): Triple<String, LocalDateTime, LocalDateTime> {
        // 既存の未使用コードを無効化
        val existingCodes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "EMAIL_VERIFY")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        return generateAndSendCode(userId, null, email, resendCount)
    }

    /**
     * 未登録ユーザー向けの認証コード送信
     * @return flowId と expiresAt のペア
     */
    @Transactional
    fun sendPreRegistration(email: String, resendCount: Int = 0): Triple<String, LocalDateTime, LocalDateTime> {
        // 既存の未使用コードを無効化 (Emailベース)
        val existingCodes = verificationCodeRepository.findByEmailAndTypeAndIsUsedFalse(email, "EMAIL_VERIFY")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        return generateAndSendCode(null, email, email, resendCount)
    }

    private fun generateAndSendCode(userId: Int?, emailForRecord: String?, toEmail: String, resendCount: Int): Triple<String, LocalDateTime, LocalDateTime> {
        val code = (100000..999999).random().toString()
        val flowId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val expiresAt = now.plusMinutes(10)

        val verificationCode = VerificationCode(
            userId = userId,
            email = toEmail,
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
        println("Email: $toEmail")
        println("=======================================")

        // メール送信を非同期で実行（APIレスポンスを即返すため）
        val subject = "【VGM】認証コードのお知らせ"
        val htmlContent = verificationCodeEmailTemplate.generate(code)
        CompletableFuture.runAsync {
            try {
                emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
            } catch (e: Exception) {
                println("Failed to send verification email to $toEmail: ${e.message}")
            }
        }

        return Triple(flowId, expiresAt, now)
    }
}
