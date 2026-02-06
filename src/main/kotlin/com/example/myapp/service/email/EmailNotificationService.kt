package com.example.myapp.service.email

import com.example.myapp.service.email.template.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

import org.springframework.scheduling.annotation.Async

@Service
class EmailNotificationService(
    private val emailSenderService: EmailSenderService,
    private val verificationCodeEmailTemplate: VerificationCodeEmailTemplate,
    private val secretKeyEmailTemplate: SecretKeyEmailTemplate,
    private val loginNotificationEmailTemplate: LoginNotificationEmailTemplate,
    private val deleteAccountEmailTemplate: DeleteAccountEmailTemplate,
    private val testEmailTemplate: TestEmailTemplate
) {

    /**
     * シークレットキーをメールで送信
     */
    @Async
    fun sendSecretKeyEmail(toEmail: String, username: String, secretKey: String) {
        val subject = "【VGM】二要素認証のセットアップ"
        val htmlContent = secretKeyEmailTemplate.generate(username, secretKey)
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * ログイン通知メールを送信
     */
    @Async
    fun sendLoginNotificationEmail(toEmail: String, username: String) {
        val subject = "【VGM】ログイン通知"
        val htmlContent = loginNotificationEmailTemplate.generate(username, LocalDateTime.now())
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * 認証コード通知メールを送信
     */
    @Async
    fun sendVerificationCodeEmail(toEmail: String, code: String) {
        val subject = "【VGM】認証コードのお知らせ"
        val htmlContent = verificationCodeEmailTemplate.generate(code)
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * アカウント削除確認メールを送信
     */
    @Async
    fun sendDeleteAccountVerificationEmail(toEmail: String, code: String) {
        val subject = "【VGM】アカウント削除の確認"
        val htmlContent = deleteAccountEmailTemplate.generate(code)
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * テスト用のシンプルなメール送信
     */
    @Async
    fun sendTestEmail(toEmail: String) {
        val subject = "【VGM】メール送信テスト"
        val htmlContent = testEmailTemplate.generate()
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }
}
