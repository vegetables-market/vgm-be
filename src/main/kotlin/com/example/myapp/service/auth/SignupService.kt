package com.example.myapp.service.auth

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.SignupRequest
import com.example.myapp.dto.auth.UserInfo
import com.example.myapp.entity.auth.UserAuthStatus
import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.UserEmail
import com.example.myapp.entity.user.UserInfoEntity
import com.example.myapp.entity.user.UserProfile
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserInfoRepository
import com.example.myapp.repository.user.UserProfileRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.util.AuthUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SignupService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val emailVerificationService: EmailVerificationService
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    @Transactional
    fun signup(request: SignupRequest): LoginResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("このユーザー名は既に使用されています")
        }
        if (userEmailRepository.existsByEmail(request.email)) {
            throw RuntimeException("このメールアドレスは既に使用されています")
        }

        val encodedPassword = passwordEncoder.encode(request.password)

        val newUser = User(
            username = request.username,
            displayName = request.display_name,
            passwordHash = encodedPassword,
            status = 1
        )
        val savedUser = userRepository.save(newUser)

        // Create AuthStatus
        val authStatus = UserAuthStatus(
            userId = savedUser.userId,
            emailVerified = false,
            hasPassword = true,
            lastAuthMethod = "PASSWORD",
            lastAuthAt = LocalDateTime.now()
        )
        userAuthStatusRepository.save(authStatus)

        // Create Email record
        val userEmail = UserEmail(
            userId = savedUser.userId,
            email = request.email,
            type = "PRIMARY",
            source = "MANUAL",
            isVerified = false,
            isPrimary = true
        )
        userEmailRepository.save(userEmail)

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

        val flowId = emailVerificationService.sendVerificationEmail(savedUser.userId, request.email)

        return LoginResponse(
            status = "REGISTERED",
            user = UserInfo(
                username = savedUser.username,
                display_name = savedUser.displayName,
                email = request.email,
                avatar_url = null,
                is_email_verified = false
            ),
            require_verification = true,
            flow_id = flowId,
            masked_email = AuthUtils.maskEmail(request.email)
        )
    }
}
