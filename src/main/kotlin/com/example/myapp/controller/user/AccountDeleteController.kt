package com.example.myapp.controller.user

import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.user.account.AccountDeletionService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

data class DeleteConfirmRequest(
    val flowId: String,
    val code: String
)

@RestController
@RequestMapping("/v1/user/account")
class AccountDeleteController(
    private val userSessionRepository: UserSessionRepository,
    private val accountDeletionService: AccountDeletionService,
    private val sensitiveActionService: com.example.myapp.service.auth.SensitiveActionService // Inject
) {

    /**
     * セッションCookieからユーザーIDを取得
     */
    private fun getUserIdFromSession(request: HttpServletRequest): Int? {
        val sessionKey = request.cookies?.find { it.name == "vgm_session" }?.value
            ?: return null

        val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
            sessionKey,
            LocalDateTime.now()
        ) ?: return null

        return session.userId
    }

    /**
     * アカウント削除リクエスト（認証フロー開始）
     * 変更点: コード送信ではなく、認証タイプ(flowIdなど)を返すのみ。
     */
    @PostMapping("/delete/request")
    fun requestDeleteAccount(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        return try {
            // SensitiveActionServiceを使用して認証フローを開始
            val initResponse = sensitiveActionService.initiateAction(userId, "delete_account")
            
            val response: Map<String, Any> = mapOf(
                "success" to true,
                "flow_id" to initResponse.flowId,
                "auth_type" to initResponse.authType.name, // "TOTP" or "EMAIL"
                "message" to initResponse.message,
                "masked_email" to (initResponse.maskedEmail ?: "")
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "User not found")))
        }
    }

    /**
     * アカウント削除実行 (Action Token必須)
     */
    @PostMapping("/delete/confirm")
    @Transactional
    fun confirmDeleteAccount(
        @RequestBody request: Map<String, String>, // flowIdではなく action_token を受け取る
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val actionToken = request["action_token"]
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Action token required"))

        return try {
            // Action Tokenを検証・消費
            val verifiedUserId = sensitiveActionService.verifyAndConsumeToken(actionToken, "delete_account")
            
            if (verifiedUserId != userId) {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Token owner mismatch"))
            }

            
            accountDeletionService.executeAccountDeletion(userId)

            // Cookieを削除
            val cookie = Cookie("vgm_session", "")
            cookie.isHttpOnly = true
            cookie.maxAge = 0
            cookie.path = "/"
            servletResponse.addCookie(cookie)

            val response: Map<String, Any> = mapOf(
                "success" to true,
                "message" to "アカウントが削除されました"
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(mapOf("error" to (e.message ?: "Invalid token")))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "Error processing request")))
        }
    }
}
