package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.auth.UserSession
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.auth.common.DataMergeService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.profile.UserInfoBuilderService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * ログイン完了ユースケース
 * ログイン成功時の最終処理（最終ログイン日時更新、セッション発行、ゲスト連携など）を行う
 */
@Service
class CompleteLogin(
    private val userRepository: UserRepository,
    private val sessionService: SessionService,
    private val dataMergeService: DataMergeService,
    private val userInfoBuilderService: UserInfoBuilderService
) {

    /**
     * ログイン完了処理を実行する
     */
    @Transactional
    operator fun invoke(
        userId: Int,
        existingSession: UserSession? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
        guestId: String? = null
    ): LoginResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalStateException("User not found") }

        // 最終ログイン日時更新
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        // セッション発行または更新
        val sessionKey = if (existingSession != null) {
            sessionService.updateLastAccessed(existingSession)
        } else {
            sessionService.createSession(userId, ipAddress, userAgent)
        }

        // ゲストデータの統合
        if (guestId != null) {
            dataMergeService.mergeGuestData(userId, guestId)
        }

        // ユーザー情報の構築
        val userInfo = userInfoBuilderService.buildUserInfo(userId)

        return LoginResponse(
            status = "AUTHENTICATED",
            user = userInfo,
            flowId = sessionKey
        )
    }
}
