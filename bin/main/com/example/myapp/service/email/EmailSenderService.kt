package com.example.myapp.service.email

import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailSenderService(
    private val mailSender: JavaMailSender,
    @Value("\${mail.from.email}") private val fromEmail: String,
    @Value("\${mail.from.name}") private val fromName: String
) {

    /**
     * 汎用的なHTMLメール送信
     */
    fun sendHtmlEmail(to: String, subject: String, htmlContent: String) {
        // 開発用ログ出力
        println("===== メール送信リクエスト =====")
        println("To: $to")
        println("Subject: $subject")
        println("Content (preview): ${htmlContent.take(100)}...")
        
        // 認証コードが含まれている場合はログに出力してデバッグしやすくする
        val codeRegex = Regex("""<p style="[^"]*">(\d{6})</p>""")
        val matchResult = codeRegex.find(htmlContent) ?: Regex("""<div class="verification-code">\s*(\d{6})\s*</div>""").find(htmlContent)
        
        if (matchResult != null) {
            println("--------------------------------")
            println("【開発用】認証コード: ${matchResult.groupValues[1]}")
            println("--------------------------------")
        }
        println("================================")

        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(fromEmail, fromName)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlContent, true) // true = HTML形式

            mailSender.send(message)

            println("メール送信成功: to=$to, subject=$subject")
        } catch (e: Exception) {
            // 開発環境ではエラーをログに出力するだけで、処理自体は続行させる
            println("メール送信エラー (開発環境では無視されます): ${e.message}")
        }
    }
}
