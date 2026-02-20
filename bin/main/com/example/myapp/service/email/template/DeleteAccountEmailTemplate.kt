package com.example.myapp.service.email.template

import org.springframework.stereotype.Component

@Component
class DeleteAccountEmailTemplate {
    
    fun generate(code: String): String {
        return """
            <html>
            <head>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }
                    .header { background-color: #d32f2f; color: white; padding: 30px 20px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .verification-code {
                        background-color: #ffebee;
                        color: #c62828;
                        font-family: 'Courier New', monospace;
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        text-align: center;
                        padding: 20px;
                        margin: 30px 0;
                        border-radius: 8px;
                        border: 2px dashed #ef9a9a;
                    }
                    .warning { 
                        background-color: #fff3e0; 
                        border-left: 4px solid #ff9800; 
                        padding: 15px; 
                        margin-top: 30px; 
                        font-size: 14px;
                    }
                    .danger-note {
                        background-color: #ffebee;
                        border-left: 4px solid #d32f2f;
                        padding: 15px;
                        margin-top: 20px;
                        font-size: 14px;
                    }
                    .footer { background-color: #f9f9f9; color: #888; font-size: 12px; text-align: center; padding: 20px; border-top: 1px solid #eee; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="margin:0; font-size: 24px;">⚠️ アカウント削除の確認</h1>
                    </div>
                    <div class="content">
                        <p>アカウント削除のリクエストを受け付けました。</p>
                        <p>この操作を続行するには、以下の認証コードを入力してください。</p>

                        <div class="verification-code">
                            $code
                        </div>

                        <p>このコードの有効期限は <strong>10分間</strong> です。</p>

                        <div class="danger-note">
                            <strong>🚨 重要：</strong><br>
                            アカウントを削除すると、すべてのデータが失われ、この操作は取り消せません。
                        </div>

                        <div class="warning">
                            <strong>⚠️ ご注意：</strong><br>
                            この操作に心当たりがない場合は、このメールを無視してください。アカウントは削除されません。
                        </div>
                    </div>
                    <div class="footer">
                        <p>このメールは自動送信されています。</p>
                        <p>&copy; 2025 VGM Application. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
