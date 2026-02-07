package com.example.myapp.controller.auth.flow

import com.example.myapp.dto.auth.flow.ResendCodeRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.email.EmailVerificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 認証コード再送信コントローラー
 */
@RestController
@RequestMapping("/v1/auth")
class ResendCodeController(
    private val emailVerificationService: EmailVerificationService
) {

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
            throw AppException(ErrorCode.AUTH_TOO_MANY_REQUESTS, e.message ?: "Too many requests")
        }
    }
}
