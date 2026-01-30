package com.example.myapp.service.auth

import com.example.myapp.entity.auth.UserAuthStatus
import com.example.myapp.entity.auth.UserOAuthConnection
import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.UserEmail
import com.example.myapp.entity.user.UserInfoEntity
import com.example.myapp.entity.user.UserProfile
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.auth.UserOAuthConnectionRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserInfoRepository
import com.example.myapp.repository.user.UserProfileRepository
import com.example.myapp.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class OAuthService(
    private val userOAuthConnectionRepository: UserOAuthConnectionRepository,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val sessionService: SessionService
) {

    @Transactional
    fun processOAuth2User(email: String, name: String, provider: String, providerUserId: String): String {
        // 1. Check if OAuth connection exists
        val existingConnection = userOAuthConnectionRepository.findByProviderAndProviderUserId(provider, providerUserId)
        
        var user: User? = null
        var isNewUser = false

        if (existingConnection != null) {
            // Existing OAuth connection - get user
            user = userRepository.findById(existingConnection.userId).orElse(null)
            if (user == null) {
                // ユーザーが削除されている場合、孤立したOAuth接続を削除して新規作成フローへ
                userOAuthConnectionRepository.delete(existingConnection)
                // user = null のまま、新規ユーザー作成フローへ進む
            } else {
                // Update last used
                existingConnection.lastUsedAt = LocalDateTime.now()
                userOAuthConnectionRepository.save(existingConnection)
            }
        }

        if (user == null) {
            // Check if user exists by email in t_user_emails
            val existingEmailRecord = userEmailRepository.findByEmail(email)
            if (existingEmailRecord != null) {
                user = userRepository.findById(existingEmailRecord.userId).orElse(null)
            }

            if (user == null) {
                // Create new user (OAuth only - no password)
                isNewUser = true
                val username = "${provider}_${UUID.randomUUID().toString().substring(0, 8)}"

                user = User(
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
            }

            // Create email record if not exists
            val existingEmail = userEmailRepository.findByEmail(email)
            val emailRecord = if (existingEmail == null) {
                val newEmail = UserEmail(
                    userId = user.userId,
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

            // Create OAuth connection
            val oauthConnection = UserOAuthConnection(
                userId = user.userId,
                provider = provider,
                providerUserId = providerUserId,
                emailId = emailRecord.emailId,
                displayName = name,
                lastUsedAt = LocalDateTime.now()
            )
            userOAuthConnectionRepository.save(oauthConnection)
        }

        // Update login info
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        // Update auth status
        val authStatus = userAuthStatusRepository.findByUserId(user.userId)
        if (authStatus != null) {
            authStatus.lastAuthMethod = provider.uppercase()
            authStatus.lastAuthAt = LocalDateTime.now()
            userAuthStatusRepository.save(authStatus)
        }

        // Create Session via SessionService
        return sessionService.createSession(user.userId, null, "OAuth2: $provider")
    }
}
