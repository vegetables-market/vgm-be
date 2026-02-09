package com.example.myapp.client

import com.example.myapp.dto.LoginRequest
import com.example.myapp.dto.RegisterRequest
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ClientRunner(private val apiClient: ApiClient) : CommandLineRunner {
    override fun run(vararg args: String?) {
        println("API ベース URL = ${System.getenv("API_BASE_URL") ?: "http://localhost:8080"}")

        try {
            println("GET /api/items を呼び出します...")
            val items = apiClient.getItems()
            println("取得したアイテム数: ${items.size}")
        } catch (e: Exception) {
            System.err.println("API 接続エラー: ${e.message}")
            System.err.println("ポートが開いていない、サーバーが起動していない、またはファイアウォール/プロキシの問題が考えられます。")
            return
        }

        try {
            println("サンプルユーザー登録 → ログインを試します...")
            val reg = apiClient.register(RegisterRequest("themeuser-client","themeuser-client@example.com","secret123"))
            println("登録結果: $reg")
            val login = apiClient.login(LoginRequest("themeuser-client","secret123"))
            println("ログイン結果: $login")
        } catch (e: Exception) {
            System.err.println("認証操作でエラー: ${e.message}")
        }
    }
}
