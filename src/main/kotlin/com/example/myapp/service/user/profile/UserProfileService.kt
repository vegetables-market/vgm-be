package com.example.myapp.service.user.profile

import com.example.myapp.entity.user.profile.UserProfile
import com.example.myapp.repository.user.UserProfileRepository
import org.springframework.stereotype.Service
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.dto.user.account.UserProfileInfo
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.transaction.annotation.Transactional

/**
 * ユーザープロフィール管理を担当するサービス
 */
@Service
class UserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * プロフィールを取得 (UserProfile Entity)
     */
    fun getProfile(userId: Int): UserProfile? {
        return userProfileRepository.findById(userId).orElse(null)
    }

    /**
     * ユーザー情報全般取得 (User Entity + AuthStatus)
     * MyAccountController用のデータを返す
     */

    fun getUserProfileInfo(userId: Int): UserProfileInfo {
        val user = userRepository.findById(userId).orElseThrow {
            AppException(ErrorCode.RESOURCE_NOT_FOUND, "ユーザーが見つかりません")
        }
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        
        return UserProfileInfo(
            userId = user.userId,
            username = user.username,
            displayName = user.displayName ?: user.username,
            email = null, // TODO: 必要であればEmailRepositoryから取得して設定
            avatarUrl = null, // TODO: 必要であればUserProfileから取得
            hasPassword = authStatus?.hasPassword ?: false,
            role = user.role,
            isEmailVerified = authStatus?.emailVerified ?: false
        )
    }

    /**
     * プロフィールを更新 (Bio, Avatar)
     */
    @Transactional
    fun updateProfile(userId: Int, profileText: String?, profileImageUrl: String?): UserProfile {
        val profile = userProfileRepository.findById(userId).orElseGet {
            UserProfile(userId = userId)
        }
        profileText?.let { profile.profileText = it }
        profileImageUrl?.let { profile.profileImageUrl = it }
        return userProfileRepository.save(profile)
    }

    /**
     * アバター画像URLを更新
     */
    @Transactional
    fun updateAvatarUrl(userId: Int, avatarUrl: String): UserProfile {
        val profile = userProfileRepository.findById(userId).orElseGet {
            UserProfile(userId = userId)
        }
        profile.profileImageUrl = avatarUrl
        return userProfileRepository.save(profile)
    }

    /**
     * プロフィールテキストを更新
     */
    @Transactional
    fun updateProfileText(userId: Int, profileText: String): UserProfile {
        if (profileText.length > 1000) {
            throw AppException(ErrorCode.INVALID_INPUT, "自己紹介は1000文字以下で入力してください")
        }
        
        val profile = userProfileRepository.findById(userId).orElseGet {
            UserProfile(userId = userId)
        }
        profile.profileText = profileText
        return userProfileRepository.save(profile)
    }

    /**
     * ユーザー名を更新
     */
    @Transactional
    fun updateUsername(userId: Int, newUsername: String, password: String?): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow {
            AppException(ErrorCode.RESOURCE_NOT_FOUND, "ユーザーが見つかりません")
        }
        
        verifyPasswordIfRequired(userId, password, user.passwordHash)

        // バリデーション
        val trimmedUsername = newUsername.trim()
        if (trimmedUsername.length < 3 || trimmedUsername.length > 20) {
            throw AppException(ErrorCode.INVALID_INPUT, "ユーザー名は3文字以上20文字以下で入力してください")
        }
        if (!trimmedUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            throw AppException(ErrorCode.INVALID_INPUT, "ユーザー名は英数字とアンダースコアのみ使用できます")
        }

        if (userRepository.existsByUsername(trimmedUsername) && user.username != trimmedUsername) {
            throw AppException(ErrorCode.INVALID_INPUT, "このユーザー名は既に使用されています")
        }

        user.username = trimmedUsername
        userRepository.save(user)

        return mapOf(
            "success" to true,
            "message" to "ユーザー名を変更しました",
            "username" to trimmedUsername
        )
    }

    /**
     * 表示名を更新
     */
    @Transactional
    fun updateDisplayName(userId: Int, newDisplayName: String, password: String?): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow {
            AppException(ErrorCode.RESOURCE_NOT_FOUND, "ユーザーが見つかりません")
        }

        verifyPasswordIfRequired(userId, password, user.passwordHash)

        val trimmedDisplayName = newDisplayName.trim()
        if (trimmedDisplayName.isEmpty()) {
            throw AppException(ErrorCode.INVALID_INPUT, "表示名を入力してください")
        }
        if (trimmedDisplayName.length > 100) {
            throw AppException(ErrorCode.INVALID_INPUT, "表示名は100文字以下で入力してください")
        }

        user.displayName = trimmedDisplayName
        userRepository.save(user)

        return mapOf(
            "success" to true,
            "message" to "表示名を変更しました",
            "displayName" to trimmedDisplayName
        )
    }

    /**
     * パスワードを変更
     */
    @Transactional
    fun changePassword(userId: Int, currentPassword: String?, newPassword: String): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow {
            AppException(ErrorCode.RESOURCE_NOT_FOUND, "ユーザーが見つかりません")
        }

        verifyPasswordIfRequired(userId, currentPassword, user.passwordHash)

        if (newPassword.length < 8) {
            throw AppException(ErrorCode.INVALID_INPUT, "パスワードは8文字以上で入力してください")
        }

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        val authStatus = userAuthStatusRepository.findByUserId(userId)
        if (authStatus != null && !authStatus.hasPassword) {
            authStatus.hasPassword = true
            userAuthStatusRepository.save(authStatus)
        }

        return mapOf(
            "success" to true,
            "message" to "パスワードを変更しました"
        )
    }

    /**
     * パスワード認証が必要かチェックし、必要なら検証する
     */
    private fun verifyPasswordIfRequired(userId: Int, password: String?, passwordHash: String?) {
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        if (authStatus?.hasPassword == true) {
            if (password.isNullOrBlank()) {
                throw AppException(ErrorCode.INVALID_INPUT, "パスワードを入力してください")
            }
            if (passwordHash != null && !passwordEncoder.matches(password, passwordHash)) {
                throw AppException(ErrorCode.INVALID_INPUT, "パスワードが正しくありません")
            }
        }
    }
}
