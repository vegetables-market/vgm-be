package com.example.myapp.controller

import com.example.myapp.service.EmailService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class SendTestEmailRequest(
    val email: String
)

data class SendSecretKeyEmailRequest(
    val email: String,
    val username: String,
    val secretKey: String
)

data class EmailResponse(
    val success: Boolean,
    val message: String
)

@RestController
@RequestMapping("/api/email")
class EmailController(
    private val emailService: EmailService
) {

    /**
     * テストメール送信
     */
    @PostMapping("/test")
    fun sendTestEmail(@RequestBody request: SendTestEmailRequest): ResponseEntity<EmailResponse> {
        return try {
            emailService.sendTestEmail(request.email)
            ResponseEntity.ok(
                EmailResponse(
                    success = true,
                    message = "テストメールを送信しました: ${request.email}"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                EmailResponse(
                    success = false,
                    message = "メール送信に失敗しました: ${e.message}"
                )
            )
        }
    }

    /**
     * シークレットキーメール送信
     */
    @PostMapping("/send-secret")
    fun sendSecretKeyEmail(@RequestBody request: SendSecretKeyEmailRequest): ResponseEntity<EmailResponse> {
        return try {
            emailService.sendSecretKeyEmail(
                toEmail = request.email,
                username = request.username,
                secretKey = request.secretKey
            )
            ResponseEntity.ok(
                EmailResponse(
                    success = true,
                    message = "シークレットキーを送信しました: ${request.email}"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                EmailResponse(
                    success = false,
                    message = "メール送信に失敗しました: ${e.message}"
                )
            )
        }
    }

    /**
     * ログイン通知メール送信
     */
    @PostMapping("/login-notification")
    fun sendLoginNotification(@RequestBody request: SendTestEmailRequest): ResponseEntity<EmailResponse> {
        return try {
            // usernameはemailから抽出（@の前の部分）
            val username = request.email.substringBefore("@")
            emailService.sendLoginNotificationEmail(request.email, username)
            ResponseEntity.ok(
                EmailResponse(
                    success = true,
                    message = "ログイン通知メールを送信しました: ${request.email}"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                EmailResponse(
                    success = false,
                    message = "メール送信に失敗しました: ${e.message}"
                )
            )
        }
    }
}
