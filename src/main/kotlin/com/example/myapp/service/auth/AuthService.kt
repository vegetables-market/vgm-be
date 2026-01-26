package com.example.myapp.service.auth

import com.example.myapp.dto.auth.LoginRequest
import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.SignupRequest
import com.example.myapp.dto.auth.UserInfo
import com.example.myapp.entity.User
import com.example.myapp.entity.UserInfoEntity
import com.example.myapp.entity.UserProfile
import com.example.myapp.entity.UserSession
import com.example.myapp.repository.auth.UserInfoRepository
import com.example.myapp.repository.auth.UserRepository
import com.example.myapp.repository.auth.UserProfileRepository
import com.example.myapp.repository.auth.UserSessionRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * 認証関連のビジネスロジック
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userSessionRepository: UserSessionRepository,
    private val emailVerificationService: EmailVerificationService
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * メールアドレスをマスキングする
     */
    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email

        val localPart = email.substring(0, atIndex)
        val domainPart = email.substring(atIndex + 1)

        val maskedLocal = if (localPart.length >= 2) {
            "${localPart.first()}******${localPart.last()}"
        } else {
            "${localPart}******"
        }

        val dotIndex = domainPart.lastIndexOf('.')
        val maskedDomain = if (dotIndex > 1) {
            val domainName = domainPart.substring(0, dotIndex)
            val tld = domainPart.substring(dotIndex)
            "${domainName.first()}*${domainName.last()}$tld"
        } else {
            domainPart
        }

        return "$maskedLocal@$maskedDomain"
    }

    /**
     * ログイン処理
     */
    @Transactional
    fun login(request: LoginRequest, ipAddress: String? = null, userAgent: String? = null): LoginResponse {
        val user = userRepository.findByUsernameOrEmail(request.username, request.username)
        
        val session = request.device_id?.let {
            userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(it, LocalDateTime.now())
        }
        val isKnownDevice = session != null && user != null && session.userId == user.userId

        val isPasswordCorrect = request.password?.let {
            user != null && passwordEncoder.matches(it, user.passwordHash)
        } ?: false

        if (request.password == null) {
            if (isKnownDevice) {
                val flowId = emailVerificationService.sendVerificationEmail(user!!.userId, user.email ?: user.username)
                return LoginResponse(
                    status = "VERIFICATION_REQUIRED",
                    user = null,
                    require_verification = true,
                    flow_id = flowId,
                    masked_email = user.email?.let { maskEmail(it) }
                )
            } else {
                return LoginResponse(
                    status = "PASSWORD_REQUIRED",
                    user = null
                )
            }
        }

        if (!isKnownDevice || !isPasswordCorrect) {
            var flowId: String? = null
            var maskedEmail: String? = null
            if (user != null) {
                flowId = emailVerificationService.sendVerificationEmail(user.userId, user.email ?: user.username)
                maskedEmail = user.email?.let { maskEmail(it) }
            }
            
            return LoginResponse(
                status = "VERIFICATION_REQUIRED",
                user = null,
                require_verification = true,
                flow_id = flowId,
                masked_email = maskedEmail
            )
        }

        user!!.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        session?.let {
            it.lastAccessedAt = LocalDateTime.now()
            userSessionRepository.save(it)
        }

        val userProfile = userProfileRepository.findById(user.userId).orElse(null)
        
        return LoginResponse(
            status = "AUTHENTICATED",
            user = UserInfo(
                user_id = user.userId,
                display_name = user.displayName,
                email = user.email,
                avatar_url = userProfile?.profileImageUrl,
                is_email_verified = user.emailVerified == 1.toShort()
            )
        )
    }

    /**
     * ログアウト処理
     */
    @Transactional
    fun logout(sessionKey: String) {
        val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(sessionKey, LocalDateTime.now())
        session?.let {
            it.isRevoked = true
            userSessionRepository.save(it)
        }
    }

    fun getUserById(userId: Int): User? {
        return userRepository.findById(userId).orElse(null)
    }

    fun getUserByIdentifier(identifier: String): User? {
        return userRepository.findByUsernameOrEmail(identifier, identifier)
    }

    @Transactional
    fun createSession(userId: Int, ipAddress: String? = null, deviceName: String? = null): String {
        val sessionKey = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()
        
        val session = UserSession(
            userId = userId,
            sessionKey = sessionKey,
            refreshTokenHash = passwordEncoder.encode(refreshToken),
            deviceName = deviceName,
            ipAddress = ipAddress,
            expiresAt = LocalDateTime.now().plusDays(30)
        )
        
        userSessionRepository.save(session)
        return sessionKey
    }

    @Transactional
    fun signup(request: SignupRequest): LoginResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("このユーザー名は既に使用されています")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw RuntimeException("このメールアドレスは既に使用されています")
        }

        val encodedPassword = passwordEncoder.encode(request.password)

        val newUser = User(
            username = request.username,
            email = request.email,
            displayName = request.display_name,
            passwordHash = encodedPassword,
            status = 1,
            emailVerified = 0
        )
        val savedUser = userRepository.save(newUser)

        val newProfile = UserProfile(
            userId = savedUser.userId,
            profileText = "はじめまして！"
        )
        userProfileRepository.save(newProfile)

        // 生年月日と性別の保存
        val genderCode: Short = when (request.gender) {
            "male" -> 1
            "female" -> 2
            "other" -> 3
            else -> 0
        }

        val birthDate = if (request.birth_year != null && request.birth_month != null && request.birth_day != null) {
            try {
                LocalDate.of(request.birth_year, request.birth_month, request.birth_day)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val userInfo = UserInfoEntity(
            userId = savedUser.userId,
            gender = genderCode,
            birthDate = birthDate
        )
        userInfoRepository.save(userInfo)

        val flowId = emailVerificationService.sendVerificationEmail(savedUser.userId, savedUser.email!!)

        return LoginResponse(
            status = "REGISTERED",
            user = UserInfo(
                user_id = savedUser.userId,
                display_name = savedUser.displayName,
                email = savedUser.email,
                avatar_url = null,
                is_email_verified = false
            ),
            require_verification = true,
            flow_id = flowId,
            masked_email = maskEmail(savedUser.email!!)
        )
    }
}
