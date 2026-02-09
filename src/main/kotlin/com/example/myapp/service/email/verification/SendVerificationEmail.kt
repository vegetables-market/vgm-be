package com.example.myapp.service.email.verification

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.VerificationCodeEmailTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

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
            email = toEmail, // Always save the email being sent to
            code = code,
            flowId = flowId,
            type = "EMAIL_VERIFY",
            expiresAt = expiresAt,
            resendCount = resendCount
        )
        // しかし VerificationCode.email は val らしいので、コンストラクタでセットする
        if (userId != null) {
             // コンストラクタで emailForRecord (which is null for registered but passed as null in call)
             // Wait, verify logic uses userEmailRepository for registered users if email in verification is null?
             // No, VerificationCode table likely stores email for both?
             // Let's check VerificationCode entity definition if possible, but I can just pass toEmail to constructor if emailForRecord is null
        }
        // Actually, in generateAndSendCode(userId, null, email, ...)
        // I passed null for emailForRecord when userId is not null.
        // I should just pass toEmail there if I want it to be saved.
        // Let's modify the construction.

        verificationCodeRepository.save(verificationCode)

        // 開発用ログ
        println("===== Verification Code Generated =====")
        println("FlowID: $flowId")
        println("Code: $code")
        println("Email: $toEmail")
        println("=======================================")

        val subject = "【VGM】認証コードのお知らせ"
        val htmlContent = verificationCodeEmailTemplate.generate(code)
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)

        return Triple(flowId, expiresAt, now)
    }
}
