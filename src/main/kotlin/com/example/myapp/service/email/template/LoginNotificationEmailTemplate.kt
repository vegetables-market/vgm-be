package com.example.myapp.service.email.template

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LoginNotificationEmailTemplate {
    
    fun generate(username: String, dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
        val formattedDateTime = dateTime.format(formatter)

        return """
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
    }
}
