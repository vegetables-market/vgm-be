package com.example.myapp.service.user

import com.example.myapp.dto.user.security.email.AddEmailRequest
import com.example.myapp.dto.user.security.email.EmailResponse
import com.example.myapp.dto.user.security.email.VerifyEmailRequest
import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.entity.user.email.UserEmail
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.service.email.EmailNotificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserEmailService(
    private val userEmailRepository: UserEmailRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailNotificationService: EmailNotificationService
) {

    /**
     * メールアドレス追加（認証コード送信）
     */
    @Transactional
    fun addEmail(userId: Int, request: AddEmailRequest): Map<String, Any> {
        val email = request.email.trim().lowercase()

        // メールアドレスのバリデーション
        if (!email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) {
            throw AppException(ErrorCode.INVALID_INPUT, "有効なメールアドレスを入力してください")
        }

        // 既に登録済みかチェック
        if (userEmailRepository.existsByEmail(email)) {
            throw AppException(ErrorCode.INVALID_INPUT, "このメールアドレスは既に登録されています")
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

        // メール送信
        emailNotificationService.sendVerificationCodeEmail(email, code)

        return mapOf(
            "success" to true,
            "flow_id" to flowId,
            "message" to "認証コードを送信しました"
        )
    }

    /**
     * メールアドレス追加の確認
     */
    @Transactional
    fun verifyAddEmail(userId: Int, request: VerifyEmailRequest): Map<String, Any> {
        // コード検証
        val verification = verificationCodeRepository.findByFlowIdAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId = request.flowId,
            code = request.code,
            type = "ADD_EMAIL",
            now = LocalDateTime.now()
        ) ?: throw AppException(ErrorCode.AUTH_CODE_INVALID, "認証コードが無効または期限切れです")

        if (verification.userId != userId) {
            throw AppException(ErrorCode.AUTH_CODE_INVALID, "認証エラー")
        }

        // コードを使用済みに
        verification.isUsed = true
        verificationCodeRepository.save(verification)

        // メールアドレスを追加
        val emailAddress = verification.email
            ?: throw AppException(ErrorCode.SYSTEM_ERROR, "メールアドレスが見つかりません")

        val newEmail = UserEmail(
            userId = userId,
            email = emailAddress,
            type = "SUB",
            source = "MANUAL",
            isPrimary = false,
            isVerified = true
        )
        userEmailRepository.save(newEmail)

        return mapOf(
            "success" to true,
            "message" to "メールアドレスを追加しました"
        )
    }

    /**
     * メールアドレス一覧取得
     */
    @Transactional(readOnly = true)
    fun getEmails(userId: Int): List<EmailResponse> {
        val emails = userEmailRepository.findByUserId(userId)
        return emails.map { email ->
            EmailResponse(
                emailId = email.emailId,
                email = email.email,
                isPrimary = email.isPrimary,
                isVerified = email.isVerified,
                createdAt = email.createdAt
            )
        }
    }

    /**
     * プライマリメールアドレスの変更
     */
    @Transactional
    fun setPrimaryEmail(userId: Int, emailId: Long): Map<String, Any> {
        val targetEmail = userEmailRepository.findById(emailId).orElse(null)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "メールアドレスが見つかりません")

        if (targetEmail.userId != userId) {
            throw AppException(ErrorCode.AUTH_FORBIDDEN, "権限がありません")
        }

        if (!targetEmail.isVerified) {
            throw AppException(ErrorCode.INVALID_INPUT, "認証済みのメールアドレスのみプライマリに設定できます")
        }

        // 現在のプライマリを解除
        val currentPrimary = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
        if (currentPrimary != null) {
            currentPrimary.isPrimary = false
            userEmailRepository.save(currentPrimary)
        }

        // 新しいプライマリを設定
        targetEmail.isPrimary = true
        userEmailRepository.save(targetEmail)

        return mapOf(
            "success" to true,
            "message" to "プライマリメールアドレスを変更しました"
        )
    }

    /**
     * メールアドレス削除
     */
    @Transactional
    fun deleteEmail(userId: Int, emailId: Long): Map<String, Any> {
        val targetEmail = userEmailRepository.findById(emailId).orElse(null)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "メールアドレスが見つかりません")

        if (targetEmail.userId != userId) {
            throw AppException(ErrorCode.AUTH_FORBIDDEN, "権限がありません")
        }

        if (targetEmail.isPrimary) {
            throw AppException(ErrorCode.INVALID_INPUT, "プライマリメールアドレスは削除できません")
        }

        // メールアドレスの数をチェック
        val emailCount = userEmailRepository.findByUserId(userId).size
        if (emailCount <= 1) {
            throw AppException(ErrorCode.INVALID_INPUT, "最低1つのメールアドレスが必要です")
        }

        userEmailRepository.delete(targetEmail)

        return mapOf(
            "success" to true,
            "message" to "メールアドレスを削除しました"
        )
    }
}
