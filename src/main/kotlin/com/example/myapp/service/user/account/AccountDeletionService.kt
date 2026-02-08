package com.example.myapp.service.user.account

import com.example.myapp.entity.auth.VerificationCode
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.auth.VerificationCodeRepository
import com.example.myapp.repository.user.email.UserEmailRepository
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

    // Removed unused methods: requestAccountDeletion, confirmAccountDeletion


    /**
     * アカウント削除実行（外部で検証済みの場合）
     */
    @Transactional
    fun executeAccountDeletion(userId: Int) {
        // ユーザーを論理削除
        markUserAsDeleted(userId)

        // 全セッションを無効化
        revokeAllSessions(userId)
    }

    /**
     * ユーザーを論理削除
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
