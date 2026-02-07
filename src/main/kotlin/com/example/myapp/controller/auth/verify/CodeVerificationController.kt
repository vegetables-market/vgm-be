package com.example.myapp.controller.auth.verify

import com.example.myapp.dto.auth.flow.VerifyCodeRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.email.EmailVerificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * コード正当性確認コントローラー
 * 主に新規登録フローなど、ユーザーIDが確定していない段階でのコード検証に使用
 */
@RestController
@RequestMapping("/v1/auth")
class CodeVerificationController(
    private val emailVerificationService: EmailVerificationService
) {

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
