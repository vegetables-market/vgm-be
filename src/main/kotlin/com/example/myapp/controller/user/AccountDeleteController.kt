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
    private val accountDeletionService: AccountDeletionService
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
     * アカウント削除リクエスト（認証コード送信）
     */
    @PostMapping("/delete/request")
    fun requestDeleteAccount(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        return try {
            val flowId = accountDeletionService.requestAccountDeletion(userId)
            val response: Map<String, Any> = mapOf(
                "success" to true,
                "flow_id" to flowId,
                "message" to "認証コードを送信しました"
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "User or email not found")))
        }
    }

    /**
     * アカウント削除確認（コード検証後に削除実行）
     */
    @PostMapping("/delete/confirm")
    @Transactional
    fun confirmDeleteAccount(
        @RequestBody request: DeleteConfirmRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        return try {
            accountDeletionService.confirmAccountDeletion(userId, request.flowId, request.code)

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
                .body(mapOf("error" to (e.message ?: "Invalid request")))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "User not found")))
        }
    }
}
