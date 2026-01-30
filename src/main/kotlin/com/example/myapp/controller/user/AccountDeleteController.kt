package com.example.myapp.controller.user

import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.service.email.EmailNotificationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import jakarta.servlet.http.Cookie

data class DeleteConfirmRequest(
    val flowId: String,
    val code: String
)

@RestController
@RequestMapping("/v1/user/account")
class AccountDeleteController(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userSessionRepository: UserSessionRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailNotificationService: EmailNotificationService
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
    @Transactional
    fun requestDeleteAccount(
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "ユーザーが見つかりません"))

        // プライマリメールを取得
        val emailRecord = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "メールアドレスが登録されていません"))

        // 既存の未使用コードを無効化
        val existingCodes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "DELETE_ACCOUNT")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        // 新しいコードを生成
        val code = (100000..999999).random().toString()
        val flowId = UUID.randomUUID().toString()

        val verificationCode = VerificationCode(
            userId = userId,
            email = emailRecord.email,
            code = code,
            flowId = flowId,
            type = "DELETE_ACCOUNT",
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )
        verificationCodeRepository.save(verificationCode)

        // 開発用ログ
        println("===== Delete Account Code Generated =====")
        println("FlowID: $flowId")
        println("Code: $code")
        println("Email: ${emailRecord.email}")
        println("==========================================")

        // メール送信
        emailNotificationService.sendDeleteAccountVerificationEmail(emailRecord.email, code)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "flow_id" to flowId,
            "message" to "認証コードを送信しました"
        ))
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

        // コード検証
        val verification = verificationCodeRepository.findByFlowIdAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId = request.flowId,
            code = request.code,
            type = "DELETE_ACCOUNT",
            now = LocalDateTime.now()
        ) ?: return ResponseEntity.badRequest()
            .body(mapOf("error" to "認証コードが無効または期限切れです"))

        // ユーザーIDの一致確認
        if (verification.userId != userId) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "認証エラー"))
        }

        // コードを使用済みに
        verification.isUsed = true
        verificationCodeRepository.save(verification)

        // ユーザーを論理削除
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "ユーザーが見つかりません"))

        user.status = 4  // 削除ステータス
        userRepository.save(user)

        // 全セッションを無効化
        val sessions = userSessionRepository.findByUserId(userId)
        sessions.forEach { session ->
            session.isRevoked = true
            userSessionRepository.save(session)
        }

        // Cookieを削除
        val cookie = Cookie("vgm_session", "")
        cookie.isHttpOnly = true
        cookie.maxAge = 0
        cookie.path = "/"
        servletResponse.addCookie(cookie)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "アカウントが削除されました"
        ))
    }
}
