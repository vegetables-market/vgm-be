package com.example.myapp.service.email.template

import org.springframework.stereotype.Component

@Component
class VerificationCodeEmailTemplate {
    
    fun generate(code: String): String {
        return """
            <html>
            <head>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }
                    .header { background-color: #3f51b5; color: white; padding: 30px 20px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .verification-code {
                        background-color: #f0f2f5;
                        color: #1a237e;
                        font-family: 'Courier New', monospace;
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        text-align: center;
                        padding: 20px;
                        margin: 30px 0;
                        border-radius: 8px;
                        border: 2px dashed #c5cae9;
                    }
                    .note { font-size: 14px; color: #666; margin-top: 20px; }
                    .warning { 
                        background-color: #fff3e0; 
                        border-left: 4px solid #ff9800; 
                        padding: 15px; 
                        margin-top: 30px; 
                        font-size: 14px;
                    }
                    .footer { background-color: #f9f9f9; color: #888; font-size: 12px; text-align: center; padding: 20px; border-top: 1px solid #eee; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="margin:0; font-size: 24px;">認証コード</h1>
                    </div>
                    <div class="content">
                        <p>VGMをご利用いただき、ありがとうございます。</p>
                        <p>お手続きを完了するために、以下の認証コードを入力してください。</p>

                        <div class="verification-code">
                            $code
                        </div>

                        <p>このコードの有効期限は <strong>20分間</strong> です。</p>

                        <div class="warning">
                            <strong>⚠️ ご注意：</strong><br>
                            このコードを他人に教えないでください。VGMのスタッフがパスワードや認証コードを尋ねることはありません。
                        </div>
                    </div>
                    <div class="footer">
                        <p>このメールに心当たりがない場合は、無視して削除してください。</p>
                        <p>&copy; 2025 VGM Application. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
