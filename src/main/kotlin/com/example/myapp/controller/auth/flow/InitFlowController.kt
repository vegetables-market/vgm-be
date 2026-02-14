package com.example.myapp.controller.auth.flow

import com.example.myapp.dto.auth.flow.InitAuthRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.email.verification.SendVerificationEmail
import com.example.myapp.util.AuthUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 認証フロー開始コントローラー
 * メールアドレスを受け取り、認証フローを開始する（コード送信など）
 */
@RestController
@RequestMapping("/v1/auth")
class InitFlowController(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val sendVerificationEmail: SendVerificationEmail
) {

    @PostMapping("/init-flow")
    fun initFlow(@RequestBody request: InitAuthRequest): ResponseEntity<Map<String, Any>> {
        // メールアドレスからユーザー検索
        val userEmail = userEmailRepository.findByEmail(request.email)
        
        val (flowId, expiresAt, createdAt) = if (userEmail != null) {
            // 既存ユーザー -> 認証コード送信
             sendVerificationEmail(userEmail.userId, request.email)
        } else {
            // 新規ユーザー -> 登録用認証コード送信
             sendVerificationEmail.sendPreRegistration(request.email)
        }

        // マスクされたメールアドレス
        val maskedEmail = AuthUtils.maskEmail(request.email)

        // 統一レスポンス
        return ResponseEntity.ok(mapOf(
            "flow" to "CHALLENGE",
            "flow_id" to flowId,
            "masked_email" to maskedEmail,
            "expires_at" to expiresAt.toString(),
            "next_resend_at" to createdAt.plusSeconds(30).toString(),
            "message" to "Verification code sent."
        ))
    }
}
