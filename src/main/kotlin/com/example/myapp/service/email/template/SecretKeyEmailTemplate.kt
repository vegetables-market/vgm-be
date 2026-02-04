package com.example.myapp.service.email.template

import org.springframework.stereotype.Component

@Component
class SecretKeyEmailTemplate {
    
    fun generate(username: String, secretKey: String): String {
        return """
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
    }
}
