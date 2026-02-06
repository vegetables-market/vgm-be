package com.example.myapp.controller.auth

import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.email.EmailVerificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class InitauthRequest(
    val email: String
)

data class VerifyCodeRequest(
    val flow_id: String,
    val code: String
)

data class ResendCodeRequest(
    val flow_id: String
)

@RestController
@RequestMapping("/v1/auth")
class AuthFlowController(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val emailVerificationService: EmailVerificationService
) {

    @PostMapping("/init-flow")
    fun initFlow(@RequestBody request: InitauthRequest): ResponseEntity<Map<String, Any>> {
        // メールアドレスからユーザー検索
        val userEmail = userEmailRepository.findByEmail(request.email)
        
        val (flowId, expiresAt, createdAt) = if (userEmail != null) {
            // 既存ユーザー -> 認証コード送信
             emailVerificationService.sendVerificationEmail(userEmail.userId, request.email)
        } else {
            // 新規ユーザー -> 登録用認証コード送信
             emailVerificationService.sendPreRegistrationVerificationEmail(request.email)
        }

        // 統一レスポンス
        return ResponseEntity.ok(mapOf(
            "flow" to "CHALLENGE",
            "flow_id" to flowId,
            "expires_at" to expiresAt.toString(),
            "next_resend_at" to createdAt.plusSeconds(30).toString(),
            "message" to "Verification code sent."
        ))
    }

    @PostMapping("/resend-code")
    fun resendCode(@RequestBody request: ResendCodeRequest): ResponseEntity<Map<String, Any>> {
        try {
            val result = emailVerificationService.resendVerificationEmail(request.flow_id)
            
            if (result != null) {
                val (newFlowId, expiresAt, createdAt) = result
                return ResponseEntity.ok(mapOf(
                    "flow_id" to newFlowId,
                    "expires_at" to expiresAt.toString(),
                    "next_resend_at" to createdAt.plusSeconds(30).toString(),
                    "message" to "Verification code resent."
                ))
            } else {
                throw AppException(ErrorCode.INVALID_INPUT, "Invalid flow_id")
            }
        } catch (e: RuntimeException) {
            if (e.message == "RESEND_LIMIT_EXCEEDED") {
                throw AppException(ErrorCode.AUTH_RESEND_LIMIT_EXCEEDED)
            }
            // レート制限などのエラー
            throw AppException(ErrorCode.AUTH_TOO_MANY_REQUESTS, e.message)
        }
    }

    @PostMapping("/verify-code")
    fun verifyCode(@RequestBody request: VerifyCodeRequest): ResponseEntity<Map<String, Any>> {
        val verification = emailVerificationService.verifyByFlowId(request.flow_id, request.code)
        
        if (verification != null) {
            return ResponseEntity.ok(mapOf(
                "verified" to true,
                "email" to verification.email!!
            ))
        } else {
            throw AppException(ErrorCode.AUTH_CODE_INVALID)
        }
    }
}
