package com.example.myapp.service.email.template

import org.springframework.stereotype.Component

@Component
class TestEmailTemplate {
    
    fun generate(): String {
        return """
            <html>
            <body>
                <h2>メール送信テスト</h2>
                <p>このメールはVGMアプリケーションからのテストメールです。</p>
                <p>メール送信機能が正常に動作しています。</p>
            </body>
            </html>
        """.trimIndent()
    }
}
