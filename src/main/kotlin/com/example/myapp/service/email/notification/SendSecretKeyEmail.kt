package com.example.myapp.service.email.notification

import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.SecretKeyEmailTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * シークレットキー送信ユースケース
 */
@Service
class SendSecretKeyEmail(
    private val emailSenderService: EmailSenderService,
    private val secretKeyEmailTemplate: SecretKeyEmailTemplate
) {

    /**
     * シークレットキーをメールで送信
     */
    @Async
    operator fun invoke(toEmail: String, username: String, secretKey: String) {
        val subject = "【VGM】二要素認証のセットアップ"
        val htmlContent = secretKeyEmailTemplate.generate(username, secretKey)
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }
}
