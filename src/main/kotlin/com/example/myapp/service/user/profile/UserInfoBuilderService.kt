package com.example.myapp.service.user.profile

import com.example.myapp.dto.auth.login.UserInfo
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserProfileRepository
import com.example.myapp.repository.user.UserRepository
import org.springframework.stereotype.Service

/**
 * UserInfo DTO の構築を担当するサービス
 * 複数箇所で一貫した UserInfo を生成するための共通ロジック
 */
@Service
class UserInfoBuilderService(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {

    /**
     * プライマリメールアドレスを取得
     * @param userId ユーザーID
     * @return プライマリメールアドレス (存在しない場合null)
     */
    private fun getPrimaryEmail(userId: Int): String? {
        return userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)?.email
    }

    /**
     * 完全な UserInfo を構築
     * @param userId ユーザーID
     * @return UserInfo DTO
     * @throws IllegalStateException ユーザーが存在しない場合
     */
    fun buildUserInfo(userId: Int): UserInfo {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalStateException("User not found")
        }

        val userProfile = userProfileRepository.findById(userId).orElse(null)
        val authStatus = userAuthStatusRepository.findByUserId(userId)

        return UserInfo(
            username = user.username,
            display_name = user.displayName,
            email = getPrimaryEmail(userId),
            avatar_url = userProfile?.profileImageUrl,
            is_email_verified = authStatus?.emailVerified ?: false
        )
    }

    /**
     * 基本的な UserInfo を構築（プロフィール情報なし）
     * @param userId ユーザーID
     * @return UserInfo DTO
     * @throws IllegalStateException ユーザーが存在しない場合
     */
    fun buildBasicUserInfo(userId: Int): UserInfo {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalStateException("User not found")
        }

        val authStatus = userAuthStatusRepository.findByUserId(userId)

        return UserInfo(
            username = user.username,
            display_name = user.displayName,
            email = getPrimaryEmail(userId),
            avatar_url = null,
            is_email_verified = authStatus?.emailVerified ?: false
        )
    }

    /**
     * UserInfo を構築（nullable版）
     * @param userId ユーザーID
     * @return UserInfo DTO (ユーザーが存在しない場合null)
     */
    fun buildUserInfoOrNull(userId: Int): UserInfo? {
        return try {
            buildUserInfo(userId)
        } catch (e: IllegalStateException) {
            null
        }
    }
}
