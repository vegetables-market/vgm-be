package com.example.myapp.service.user.account

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.email.EmailNotificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * アカウント削除処理を担当するサービス
 */
@Service
class AccountDeletionService(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userSessionRepository: UserSessionRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailNotificationService: EmailNotificationService
) {

    /**
     * アカウント削除リクエスト（認証コード送信）
     * @param userId ユーザーID
     * @return flowId
     */
    @Transactional
    fun requestAccountDeletion(userId: Int): String {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalStateException("User not found")
        }

        // プライマリメールを取得
        val emailRecord = userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)
            ?: throw IllegalStateException("Primary email not found")

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

        return flowId
    }

    /**
     * アカウント削除確認（コード検証後に削除実行）
     * @param userId ユーザーID
     * @param flowId フローID
     * @param code 認証コード
     */
    @Transactional
    fun confirmAccountDeletion(userId: Int, flowId: String, code: String) {
        // コード検証
        val verification = verificationCodeRepository.findByFlowIdAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
            flowId = flowId,
            code = code,
            type = "DELETE_ACCOUNT",
            now = LocalDateTime.now()
        ) ?: throw IllegalArgumentException("Invalid or expired verification code")

        // ユーザーIDの一致確認
        if (verification.userId != userId) {
            throw IllegalArgumentException("User ID mismatch")
        }

        // コードを使用済みに
        verification.isUsed = true
        verificationCodeRepository.save(verification)

        // ユーザーを論理削除
        markUserAsDeleted(userId)

        // 全セッションを無効化
        revokeAllSessions(userId)
    }

    /**
     * ユーザーを論理削除
     * @param userId ユーザーID
     */
    private fun markUserAsDeleted(userId: Int) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalStateException("User not found")
        }

        user.status = 4  // 削除ステータス
        userRepository.save(user)
    }

    /**
     * 全セッションを無効化
     * @param userId ユーザーID
     */
    private fun revokeAllSessions(userId: Int) {
        val sessions = userSessionRepository.findByUserId(userId)
        sessions.forEach { session ->
            session.isRevoked = true
            userSessionRepository.save(session)
        }
    }
}
