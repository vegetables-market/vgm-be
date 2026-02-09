package com.example.myapp.service.email.notification

import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.TestEmailTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * テストメール送信ユースケース
 */
@Service
class SendTestEmail(
    private val emailSenderService: EmailSenderService,
    private val testEmailTemplate: TestEmailTemplate
) {

    /**
     * テスト用のシンプルなメール送信
     */
    @Async
    operator fun invoke(toEmail: String) {
        val subject = "【VGM】メール送信テスト"
        val htmlContent = testEmailTemplate.generate()
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }
}
