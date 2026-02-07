package com.example.myapp.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.Date
import com.google.auth.oauth2.AccessToken

@Configuration
class FirebaseConfig(
    @Value("\${firebase.credentials.json:}") private val credentialsJsonBase64: String,
    @Value("\${firebase.credentials.path:}") private val credentialsPath: String
) {

    @Bean
    fun firebaseApp(): FirebaseApp {
        val optionsBuilder = FirebaseOptions.builder()

        if (credentialsJsonBase64.isNotBlank()) {
            // Base64エンコードされたJSONが指定されている場合（環境変数用）
            try {
                val decodedJson = Base64.getDecoder().decode(credentialsJsonBase64)
                val stream = ByteArrayInputStream(decodedJson)
                optionsBuilder.setCredentials(GoogleCredentials.fromStream(stream))
            } catch (e: Exception) {
                throw IllegalStateException("Failed to decode firebase credentials JSON", e)
            }
        } else if (credentialsPath.isNotBlank()) {
            // パスが指定されている場合
            try {
                val file = java.io.File(credentialsPath)
                println("Loading Firebase credentials from: ${file.absolutePath}") // デバッグ用ログ
                if (!file.exists()) {
                    throw java.io.FileNotFoundException("File not found: ${file.absolutePath}")
                }
                val stream = java.io.FileInputStream(file)
                optionsBuilder.setCredentials(GoogleCredentials.fromStream(stream))
            } catch (e: Exception) {
                 throw IllegalStateException("Failed to load firebase credentials from path: $credentialsPath", e)
            }
        } else {
            // 何も指定がない場合
            println("No Firebase credentials provided. Trying ADC...")
            try {
                optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault())
            } catch (e: Exception) {
                println("Failed to load ADC: ${e.message}. Using dummy credentials for testing/local.")
                // テストやローカル環境でADCがない場合のフォールバック
                // 注意: これを使用すると実際のFirebase操作は失敗します
                optionsBuilder.setCredentials(GoogleCredentials.create(AccessToken("dummy-token", Date())))
            }
        }
        
        // 既に初期化されていたらそれを返す
        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(optionsBuilder.build())
        } else {
            FirebaseApp.getInstance()
        }
    }
}
