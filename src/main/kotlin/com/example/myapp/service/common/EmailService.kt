package com.example.myapp.service.common

import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${mail.from.email}") private val fromEmail: String,
    @Value("\${mail.from.name}") private val fromName: String
) {

    /**
     * シークレットキーをメールで送信
     */
    fun sendSecretKeyEmail(toEmail: String, username: String, secretKey: String) {
        val subject = "【VGM】二要素認証のセットアップ"
        val htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                    .secret-key {
                        background-color: #e8f5e9;
                        padding: 15px;
                        border-left: 4px solid #4CAF50;
                        font-family: 'Courier New', monospace;
                        font-size: 18px;
                        font-weight: bold;
                        letter-spacing: 2px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 10px;
                        margin: 20px 0;
                    }
                    .footer { color: #777; font-size: 12px; text-align: center; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>二要素認証のセットアップ</h1>
                    </div>
                    <div class="content">
                        <p>こんにちは、<strong>${username}</strong> 様</p>
                        <p>二要素認証が有効化されました。以下のシークレットキーをGoogle Authenticatorアプリに手動で入力してください。</p>

                        <div class="secret-key">
                            ${secretKey}
                        </div>

                        <h3>セットアップ手順：</h3>
                        <ol>
                            <li>Google Authenticatorアプリを開く</li>
                            <li>「+」ボタンをタップ</li>
                            <li>「セットアップキーを入力」を選択</li>
                            <li>アカウント名に「VGM - ${username}」と入力</li>
                            <li>上記のシークレットキーを入力</li>
                            <li>「追加」をタップ</li>
                        </ol>

                        <div class="warning">
                            <strong>⚠️ セキュリティに関する注意：</strong>
                            <ul>
                                <li>このメールは誰にも転送しないでください</li>
                                <li>シークレットキーは安全な場所に保管してください</li>
                                <li>このメールを確認後、削除することをお勧めします</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>このメールは自動送信されています。返信しないでください。</p>
                        <p>&copy; 2025 VGM Application. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * ログイン通知メールを送信
     */
    fun sendLoginNotificationEmail(toEmail: String, username: String) {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
        val formattedDateTime = now.format(formatter)

        val subject = "【VGM】ログイン通知"
        val htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                    .info-box {
                        background-color: #e3f2fd;
                        padding: 15px;
                        border-left: 4px solid #2196F3;
                        margin: 15px 0;
                    }
                    .warning {
                        background-color: #ffebee;
                        border-left: 4px solid #f44336;
                        padding: 10px;
                        margin: 20px 0;
                    }
                    .footer { color: #777; font-size: 12px; text-align: center; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>ログイン通知</h1>
                    </div>
                    <div class="content">
                        <p>こんにちは、<strong>${username}</strong> 様</p>
                        <p>あなたのアカウントでログインが検出されました。</p>

                        <div class="info-box">
                            <strong>ログイン情報：</strong><br>
                            日時: ${formattedDateTime}
                        </div>

                        <div class="warning">
                            <strong>⚠️ 重要：</strong><br>
                            このログインに心当たりがない場合は、すぐに以下の対応を行ってください：
                            <ul>
                                <li>パスワードを変更する</li>
                                <li>二要素認証を有効にする</li>
                                <li>不審なアクティビティがないか確認する</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>このメールは自動送信されています。返信しないでください。</p>
                        <p>&copy; 2025 VGM Application. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * テスト用のシンプルなメール送信
     */
    fun sendTestEmail(toEmail: String) {
        val subject = "【VGM】メール送信テスト"
        val htmlContent = """
            <html>
            <body>
                <h2>メール送信テスト</h2>
                <p>このメールはVGMアプリケーションからのテストメールです。</p>
                <p>メール送信機能が正常に動作しています。</p>
            </body>
            </html>
        """.trimIndent()

        sendHtmlEmail(toEmail, subject, htmlContent)
    }

    /**
     * 汎用的なHTMLメール送信
     */
    fun sendHtmlEmail(to: String, subject: String, htmlContent: String) {
        // 開発用ログ出力（メール送信失敗時でも確認できるように）
        println("===== メール送信リクエスト =====")
        println("To: $to")
        println("Subject: $subject")
        println("Content (preview): ${htmlContent.take(100)}...")
        
        // 認証コードが含まれている場合はログに出力してデバッグしやすくする
        val codeRegex = Regex("""<p style="[^"]*">(\d{6})</p>""")
        val matchResult = codeRegex.find(htmlContent)
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
            // 開発環境ではエラーをログに出力するだけで、処理自体は続行させる（例外を投げない）
            // 本番環境では適切にエラーハンドリングする必要があります
            println("メール送信エラー (開発環境では無視されます): ${e.message}")
            // throw RuntimeException("メール送信に失敗しました: ${e.message}", e)
        }
    }
}
