package com.example.myapp.controller.auth.verify

import com.example.myapp.dto.auth.verify.VerifyAuthRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.AuthCodeVerificationService
import com.example.myapp.service.auth.SensitiveActionService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 重要アクション検証コントローラー
 * 重要アクション実行前の再認証でアクショントークンを発行する
 */
@RestController
@RequestMapping("/v1/auth")
class ActionVerificationController(
    private val sensitiveActionService: SensitiveActionService,
    private val authCodeVerificationService: AuthCodeVerificationService
) {

    @PostMapping("/verify-action")
    fun verifyAction(
        @RequestBody request: VerifyAuthRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            // actionパラメータは必須
            if (request.action == null) {
                throw AppException(ErrorCode.INVALID_INPUT, "Action parameter is required")
            }

            // 認証コードを検証してユーザーIDを取得
            val userId = authCodeVerificationService.verifyAuthCode(request.method, request.identifier, request.code)
                ?: throw AppException(ErrorCode.AUTH_CODE_INVALID, "Invalid or expired code")

            // アクショントークンを発行
            val actionToken = sensitiveActionService.issueActionToken(userId, request.action)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "action_token" to actionToken,
                "user" to mapOf("user_id" to userId),
                "action" to request.action
            ))

        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException(ErrorCode.AUTH_CODE_INVALID, e.message ?: "認証に失敗しました")
        }
    }
}
