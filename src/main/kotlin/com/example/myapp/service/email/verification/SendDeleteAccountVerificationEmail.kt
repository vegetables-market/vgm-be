package com.example.myapp.service.email.verification

import com.example.myapp.service.email.EmailSenderService
import com.example.myapp.service.email.template.DeleteAccountEmailTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * アカウント削除確認メール送信ユースケース
 */
@Service
class SendDeleteAccountVerificationEmail(
    private val emailSenderService: EmailSenderService,
    private val deleteAccountEmailTemplate: DeleteAccountEmailTemplate
) {

    /**
     * アカウント削除確認メールを送信
     */
    @Async
    operator fun invoke(toEmail: String, code: String) {
        val subject = "【VGM】アカウント削除の確認"
        val htmlContent = deleteAccountEmailTemplate.generate(code)
        emailSenderService.sendHtmlEmail(toEmail, subject, htmlContent)
    }
}
