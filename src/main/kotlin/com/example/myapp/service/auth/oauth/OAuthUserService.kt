package com.example.myapp.service.auth.oauth

import com.example.myapp.entity.auth.UserAuthStatus
import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.email.UserEmail
import com.example.myapp.entity.user.profile.UserInfoEntity
import com.example.myapp.entity.user.profile.UserProfile
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserInfoRepository
import com.example.myapp.repository.user.UserProfileRepository
import com.example.myapp.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * OAuth経由のユーザー管理を担当するサービス
 */
@Service
class OAuthUserService(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {

    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス
     * @return ユーザー (存在しない場合null)
     */
    fun findUserByEmail(email: String): User? {
        val emailRecord = userEmailRepository.findByEmail(email) ?: return null
        return userRepository.findById(emailRecord.userId).orElse(null)
    }

    /**
     * 新規OAuthユーザーを作成
     * User, UserProfile, UserInfo, UserAuthStatus を一括作成
     * @param email メールアドレス
     * @param name 表示名
     * @param provider プロバイダ名
     * @return 作成されたユーザー
     */
    @Transactional
    fun createOAuthUser(email: String, name: String, provider: String): User {
        val username = "${provider}_${UUID.randomUUID().toString().substring(0, 8)}"

        // Create User
        var user = User(
            username = username,
            displayName = name,
            passwordHash = null,  // No password for OAuth users
            status = 2  // Active (OAuth verified)
        )
        user = userRepository.save(user)

        // Create Profile
        val profile = UserProfile(
            userId = user.userId,
            profileText = ""
        )
        userProfileRepository.save(profile)

        // Create UserInfo
        val userInfo = UserInfoEntity(
            userId = user.userId,
            gender = 0,
            birthDate = null
        )
        userInfoRepository.save(userInfo)

        // Create AuthStatus
        val authStatus = UserAuthStatus(
            userId = user.userId,
            emailVerified = true,  // OAuth email is verified
            hasPassword = false,
            lastAuthMethod = provider.uppercase(),
            lastAuthAt = LocalDateTime.now()
        )
        userAuthStatusRepository.save(authStatus)

        return user
    }

    /**
     * OAuth用メールレコードを作成または取得
     * @param userId ユーザーID
     * @param email メールアドレス
     * @param provider プロバイダ名
     * @param isNewUser 新規ユーザーかどうか
     * @return メールレコード
     */
    @Transactional
    fun ensureEmailRecord(
        userId: Int,
        email: String,
        provider: String,
        isNewUser: Boolean
    ): UserEmail {
        val existingEmail = userEmailRepository.findByEmail(email)
        
        return if (existingEmail == null) {
            val newEmail = UserEmail(
                userId = userId,
                email = email,
                type = if (isNewUser) "PRIMARY" else "OAUTH",
                source = provider.uppercase(),
                isVerified = true,
                isPrimary = isNewUser
            )
            userEmailRepository.save(newEmail)
        } else {
            existingEmail
        }
    }

    /**
     * ログイン情報を更新
     * lastLoginAt と AuthStatus を更新
     * @param userId ユーザーID
     * @param provider プロバイダ名
     */
    @Transactional
    fun updateLoginInfo(userId: Int, provider: String) {
        // Update User last login
        val user = userRepository.findById(userId).orElse(null)
        if (user != null) {
            user.lastLoginAt = LocalDateTime.now()
            userRepository.save(user)
        }

        // Update AuthStatus
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        if (authStatus != null) {
            authStatus.lastAuthMethod = provider.uppercase()
            authStatus.lastAuthAt = LocalDateTime.now()
            userAuthStatusRepository.save(authStatus)
        }
    }
}
