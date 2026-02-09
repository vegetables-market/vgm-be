package com.example.myapp.service.email.notification

import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.LoginNotificationEmailTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * ログイン通知送信ユースケース
 */
@Service
class SendLoginNotification(
    private val emailSenderService: EmailSenderService,
    private val loginNotificationEmailTemplate: LoginNotificationEmailTemplate
) {

    /**
     * ログイン通知メールを送信
     */
    @Async
    operator fun invoke(toEmail: String, username: String) {
        val subject = "【VGM】ログイン通知"
        val htmlContent = loginNotificationEmailTemplate.generate(username, LocalDateTime.now())
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }
}
