package com.example.myapp.controller.user.email

import com.example.myapp.dto.user.email.AddEmailRequest
import com.example.myapp.dto.user.email.VerifyEmailRequest
import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.entity.user.UserEmail
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.service.email.EmailNotificationService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/v1/user/emails")
class EmailCreateController(
    private val userEmailRepository: UserEmailRepository,
    private val userSessionRepository: UserSessionRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailNotificationService: EmailNotificationService
) {

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
     * メールアドレス追加（認証コード送信）
     */
    @PostMapping
    @Transactional
    fun addEmail(
        @RequestBody request: AddEmailRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        val email = request.email.trim().lowercase()

        // メールアドレスのバリデーション
        if (!email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "有効なメールアドレスを入力してください"))
        }

        // 既に登録済みかチェック
        if (userEmailRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "このメールアドレスは既に登録されています"))
        }

        // 既存の未使用コードを無効化
        val existingCodes = verificationCodeRepository.findByUserIdAndTypeAndIsUsedFalse(userId, "ADD_EMAIL")
        existingCodes.forEach {
            it.isUsed = true
            verificationCodeRepository.save(it)
        }

        // 認証コード生成
        val code = (100000..999999).random().toString()
        val flowId = UUID.randomUUID().toString()

        val verificationCode = VerificationCode(
            userId = userId,
            email = email,
            code = code,
            flowId = flowId,
            type = "ADD_EMAIL",
            expiresAt = LocalDateTime.now().plusMinutes(20)
        )
        verificationCodeRepository.save(verificationCode)

        // 開発用ログ
        println("===== Add Email Code Generated =====")
        println("FlowID: $flowId")
        println("Code: $code")
        println("Email: $email")
        println("=====================================")

        // メール送信
        emailNotificationService.sendVerificationCodeEmail(email, code)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "flow_id" to flowId,
            "message" to "認証コードを送信しました"
        ))
    }

    /**
     * メールアドレス追加の確認
     */
    @PostMapping("/verify")
    @Transactional
    fun verifyAddEmail(
        @RequestBody request: VerifyEmailRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserIdFromSession(servletRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "ログインが必要です"))

        // コード検証
        val verification = verificationCodeRepository.findByFlowIdAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId = request.flowId,
            code = request.code,
            type = "ADD_EMAIL",
            now = LocalDateTime.now()
        ) ?: return ResponseEntity.badRequest()
            .body(mapOf("error" to "認証コードが無効または期限切れです"))

        if (verification.userId != userId) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "認証エラー"))
        }

        // コードを使用済みに
        verification.isUsed = true
        verificationCodeRepository.save(verification)

        // メールアドレスを追加
        val emailAddress = verification.email ?: return ResponseEntity.badRequest()
            .body(mapOf("error" to "メールアドレスが見つかりません"))

        val newEmail = UserEmail(
            userId = userId,
            email = emailAddress,
            type = "SUB",
            source = "MANUAL",
            isPrimary = false,
            isVerified = true
        )
        userEmailRepository.save(newEmail)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "メールアドレスを追加しました"
        ))
    }
}
