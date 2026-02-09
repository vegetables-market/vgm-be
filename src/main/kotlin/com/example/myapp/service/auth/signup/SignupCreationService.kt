package com.example.myapp.service.auth.signup

import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.entity.auth.UserAuthStatus
import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.email.UserEmail
import com.example.myapp.entity.user.profile.UserInfoEntity
import com.example.myapp.entity.user.profile.UserProfile
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.repository.user.profile.UserInfoRepository
import com.example.myapp.repository.user.profile.UserProfileRepository
import com.example.myapp.repository.user.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 登録用ユーザー作成サービス
 * ユーザー登録に必要な各種エンティティの生成と保存を担当する
 */
@Service
class SignupCreationService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    data class SignupResult(
        val user: User,
        val authStatus: UserAuthStatus,
        val userEmail: UserEmail,
        val userProfile: UserProfile,
        val userInfo: UserInfoEntity
    )

    /**
     * ユーザーおよび関連エンティティを作成・保存する
     *
     * @param request 登録リクエスト
     * @param isVerified 事前認証済みかどうか
     * @return 作成されたエンティティ情報を含む SignupResult
     */
    @Transactional
    fun createUser(request: SignupRequest, isVerified: Boolean): SignupResult {
        val encodedPassword = passwordEncoder.encode(request.password)

        // User
        val newUser = User(
            username = request.username,
            displayName = request.displayName,
            passwordHash = encodedPassword,
            status = if (isVerified) 2 else 1 // 2=Active, 1=Pending
        )
        val savedUser = userRepository.save(newUser)

        // AuthStatus
        val authStatus = UserAuthStatus(
            userId = savedUser.userId,
            emailVerified = isVerified,
            hasPassword = true,
            lastAuthMethod = "PASSWORD",
            lastAuthAt = LocalDateTime.now()
        )
        userAuthStatusRepository.save(authStatus)

        // Email
        val userEmail = UserEmail(
            userId = savedUser.userId,
            email = request.email,
            type = "PRIMARY",
            source = "MANUAL",
            isVerified = isVerified,
            isPrimary = true
        )
        userEmailRepository.save(userEmail)

        // Profile
        val newProfile = UserProfile(
            userId = savedUser.userId,
            profileText = "はじめまして！"
        )
        userProfileRepository.save(newProfile)

        // UserInfo (Gender/BirthDate)
        val genderCode: Short = when (request.gender) {
            "male" -> 1
            "female" -> 2
            "other" -> 3
            else -> 0
        }

        val birthDate = if (request.birthYear != null && request.birthMonth != null && request.birthDay != null) {
            try {
                LocalDate.of(request.birthYear, request.birthMonth, request.birthDay)
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

        return SignupResult(savedUser, authStatus, userEmail, newProfile, userInfo)
    }
}
