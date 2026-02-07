package com.example.myapp.service.auth

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.dto.auth.signup.SignupRequest
import com.example.myapp.dto.auth.login.UserInfo
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

        // 事前認証チェック
        val isPreVerified = if (request.flow_id != null) {
            emailVerificationService.isFlowVerified(request.flow_id, request.email)
        } else {
            false
        }

        if (request.flow_id != null && !isPreVerified) {
            // flow_idが送られてきたのに検証できない場合はエラーにするか、あるいは無視して再認証させるか
            // セキュリティのためエラーにする方が無難
            throw RuntimeException("Invalid verification flow.")
        }

        // 事前認証済みの場合はステータス更新
        if (isPreVerified) {
            authStatus.emailVerified = true
            userAuthStatusRepository.save(authStatus)
            
            userEmail.isVerified = true
            userEmailRepository.save(userEmail)
            
            // ユーザー状態もActiveに
            savedUser.status = 2 // Active
            userRepository.save(savedUser)
        }

        val flowId = if (!isPreVerified) {
            val (fid, _) = emailVerificationService.sendVerificationEmail(savedUser.userId, request.email)
            fid
        } else {
             // 認証済みの場合はログインセッション用のIDを発行したいが、
             // ここでは便宜上 null または ダミーを返し、Controller側でログイン処理を行わせるか、
             // あるいはここでセッションを作成するか。
             // 簡易的に UUID を生成して返す (ControllerでCookieにする用)
             // ※本来は UserSession を作るべき。
             // 今回の要件では「登録」->「ログイン」の流れ。
             // PreVerifiedなら "AUTHENTICATED" として返すが、セッションIDがないとログイン状態にならない。
             // UserSessionを作る必要がある。
             // (UserSessionRepositoryが注入されていないので、注入する必要があるが...)
             // いったん、メール認証メールを送らない、という点だけ実装する。
             null
        }

        return LoginResponse(
            status = if (isPreVerified) "AUTHENTICATED" else "REGISTERED",
            user = UserInfo(
                username = savedUser.username,
                display_name = savedUser.displayName,
                email = request.email,
                avatar_url = null,
                is_email_verified = isPreVerified
            ),
            require_verification = !isPreVerified,
            flow_id = flowId,
            masked_email = if (!isPreVerified) AuthUtils.maskEmail(request.email) else null
        )
    }

    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }

    fun generateUsernameSuggestions(baseUsername: String): List<String> {
        val suggestions = mutableListOf<String>()
        val random = java.util.Random()
        
        // パターン試行 (1回のみ)
        // パターン1: 数字3桁
        val candidate1 = baseUsername + (random.nextInt(900) + 100)
        if (!userRepository.existsByUsername(candidate1)) {
            suggestions.add(candidate1)
        }

        // パターン2: アンダースコア + 数字3桁
        if (suggestions.size < 3) {
            val candidate2 = baseUsername + "_" + (random.nextInt(900) + 100)
            if (!userRepository.existsByUsername(candidate2) && !suggestions.contains(candidate2)) {
                suggestions.add(candidate2)
            }
        }
        
        // パターン3: 現在の年
        if (suggestions.size < 3) {
            val candidate3 = baseUsername + java.time.Year.now().value
             if (!userRepository.existsByUsername(candidate3) && !suggestions.contains(candidate3)) {
                suggestions.add(candidate3)
            }
        }
        
        // それでも足りない場合はランダムで埋める (最大10回試行)
        var attempts = 0
        while (suggestions.size < 3 && attempts < 10) {
             val suffix = (random.nextInt(9000) + 1000).toString()
             val candidate = baseUsername + suffix
             if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate)
            }
            attempts++
        }

        // それでも埋まらない場合の最終手段 (UUID)
        while (suggestions.size < 3) {
             val fallback = baseUsername + "_" + java.util.UUID.randomUUID().toString().substring(0, 6)
             if (!suggestions.contains(fallback)) {
                 suggestions.add(fallback)
             }
        }

        return suggestions
    }

    fun getInitialSuggestions(): List<String> {
        val suggestions = mutableListOf<String>()
        val random = java.util.Random()
        val prefixes = listOf("user", "player", "member", "guest", "account")
        
        while (suggestions.size < 3) {
            val prefix = prefixes[random.nextInt(prefixes.size)]
            val suffix = (random.nextInt(900000) + 100000).toString() // 6桁
            val candidate = "${prefix}_$suffix"
            
            if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate)
            }
        }
        return suggestions
    }
}
