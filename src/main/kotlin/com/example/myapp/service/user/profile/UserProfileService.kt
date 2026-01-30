package com.example.myapp.service.user.profile

import com.example.myapp.entity.user.UserProfile
import com.example.myapp.repository.user.UserProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ユーザープロフィール管理を担当するサービス
 */
@Service
class UserProfileService(
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * プロフィールを取得
     * @param userId ユーザーID
     * @return プロフィール (存在しない場合null)
     */
    fun getProfile(userId: Int): UserProfile? {
        return userProfileRepository.findById(userId).orElse(null)
    }

    /**
     * プロフィールを更新
     * @param userId ユーザーID
     * @param profileText プロフィールテキスト
     * @param profileImageUrl プロフィール画像URL
     * @return 更新されたプロフィール
     */
    @Transactional
    fun updateProfile(userId: Int, profileText: String?, profileImageUrl: String?): UserProfile {
        val existingProfile = userProfileRepository.findById(userId).orElseGet {
            // プロフィールが存在しない場合は新規作成
            UserProfile(userId = userId)
        }

        val updatedProfile = existingProfile.copy(
            profileText = profileText ?: existingProfile.profileText,
            profileImageUrl = profileImageUrl ?: existingProfile.profileImageUrl
        )

        return userProfileRepository.save(updatedProfile)
    }

    /**
     * アバター画像URLを更新
     * @param userId ユーザーID
     * @param avatarUrl アバター画像URL
     * @return 更新されたプロフィール
     */
    @Transactional
    fun updateAvatarUrl(userId: Int, avatarUrl: String): UserProfile {
        val existingProfile = userProfileRepository.findById(userId).orElseGet {
            UserProfile(userId = userId)
        }

        val updatedProfile = existingProfile.copy(profileImageUrl = avatarUrl)
        return userProfileRepository.save(updatedProfile)
    }

    /**
     * プロフィールテキストを更新
     * @param userId ユーザーID
     * @param profileText プロフィールテキスト
     * @return 更新されたプロフィール
     */
    @Transactional
    fun updateProfileText(userId: Int, profileText: String): UserProfile {
        val existingProfile = userProfileRepository.findById(userId).orElseGet {
            UserProfile(userId = userId)
        }

        val updatedProfile = existingProfile.copy(profileText = profileText)
        return userProfileRepository.save(updatedProfile)
    }
}
