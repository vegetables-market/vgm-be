package com.example.myapp.service.auth.signup

import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.entity.auth.UserAuthStatus
import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.email.UserEmail
import com.example.myapp.entity.user.profile.UserInfoEntity
import com.example.myapp.entity.user.profile.UserProfile
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import com.example.myapp.repository.user.profile.UserInfoRepository
import com.example.myapp.repository.user.profile.UserProfileRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ユーザー作成ユースケース
 * ユーザー登録に必要な各種エンティティの生成と保存を担当する
 */
@Service
class CreateVerifiedUser(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    data class Result(
        val user: User,
        val authStatus: UserAuthStatus,
        val userEmail: UserEmail,
        val userProfile: UserProfile,
        val userInfo: UserInfoEntity
    )

    /**
     * ユーザーおよび関連エンティティを作成・保存する
     */
    @Transactional
    operator fun invoke(request: SignupRequest, isVerified: Boolean): Result {
        val encodedPassword = passwordEncoder.encode(request.password)

        // ユーザー
        val newUser = User(
            username = request.username,
            displayName = request.displayName,
            passwordHash = encodedPassword,
            status = if (isVerified) 2 else 1 // 2=Active, 1=Pending
        )
        val savedUser = userRepository.save(newUser)

        // 認証ステータス
        val authStatus = UserAuthStatus(
            userId = savedUser.userId,
            emailVerified = isVerified,
            hasPassword = true,
            lastAuthMethod = "PASSWORD",
            lastAuthAt = LocalDateTime.now()
        )
        userAuthStatusRepository.save(authStatus)

        // メールアドレス
        val userEmail = UserEmail(
            userId = savedUser.userId,
            email = request.email,
            type = "PRIMARY",
            source = "MANUAL",
            isVerified = isVerified,
            isPrimary = true
        )
        userEmailRepository.save(userEmail)

        // プロフィール
        val newProfile = UserProfile(
            userId = savedUser.userId,
            profileText = "はじめまして！"
        )
        userProfileRepository.save(newProfile)

        // ユーザー情報 (性別/生年月日)
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

        return Result(savedUser, authStatus, userEmail, newProfile, userInfo)
    }
}
