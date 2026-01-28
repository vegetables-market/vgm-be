package com.example.myapp.service.auth

import com.example.myapp.dto.auth.LoginRequest
import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.SignupRequest
import com.example.myapp.dto.auth.UserInfo
import com.example.myapp.entity.user.User
import com.example.myapp.entity.user.UserInfoEntity
import com.example.myapp.entity.user.UserProfile
import com.example.myapp.entity.auth.UserSession
import com.example.myapp.repository.user.UserInfoRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.UserProfileRepository
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
    private val emailVerificationService: EmailVerificationService,
    private val mfaService: com.example.myapp.service.auth.MfaService
) {
    private val passwordEncoder = BCryptPasswordEncoder()
    private val mfaTokenSecret = "vgm-mfa-token-secret-key-change-in-production-environment" // TODO: Use environment variable

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
     * MFAトークンを生成
     * format: Base64(userId:expiry:signature)
     */
    private fun generateMfaToken(userId: Int): String {
        val expiry = System.currentTimeMillis() + 300000 // 5 minutes
        val data = "$userId:$expiry"
        val hmac = javax.crypto.Mac.getInstance("HmacSHA256")
        val secretKey = javax.crypto.spec.SecretKeySpec(mfaTokenSecret.toByteArray(), "HmacSHA256")
        hmac.init(secretKey)
        val signature = java.util.Base64.getEncoder().encodeToString(hmac.doFinal(data.toByteArray()))
        return java.util.Base64.getEncoder().encodeToString("$data:$signature".toByteArray())
    }

    /**
     * MFAトークンを検証
     */
    private fun validateMfaToken(token: String): Int? {
        try {
            val decoded = String(java.util.Base64.getDecoder().decode(token))
            val parts = decoded.split(":")
            if (parts.size != 3) return null

            val userId = parts[0].toInt()
            val expiry = parts[1].toLong()
            val signature = parts[2]

            if (System.currentTimeMillis() > expiry) return null

            val data = "$userId:$expiry"
            val hmac = javax.crypto.Mac.getInstance("HmacSHA256")
            val secretKey = javax.crypto.spec.SecretKeySpec(mfaTokenSecret.toByteArray(), "HmacSHA256")
            hmac.init(secretKey)
            val expectedSignature = java.util.Base64.getEncoder().encodeToString(hmac.doFinal(data.toByteArray()))

            return if (signature == expectedSignature) userId else null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * ログイン処理
     */
    @Transactional
    fun login(request: LoginRequest, ipAddress: String? = null, userAgent: String? = null): LoginResponse {
        val user = userRepository.findByUsernameOrEmail(request.username, request.username)
        
        if (request.password == null) {
            return LoginResponse(
                status = "PASSWORD_REQUIRED",
                user = null
            )
        }

        // パスワードチェック：ユーザーが存在しない、またはパスワードが一致しない場合は即座にエラー
        val isPasswordCorrect = user != null && passwordEncoder.matches(request.password, user.passwordHash)
        if (!isPasswordCorrect) {
             return LoginResponse(
                status = "INVALID_CREDENTIALS", // 具体的な理由は返さない
                user = null,
                message = "ユーザー名またはパスワードが間違っています"
            )
        }
        
        // ここから先はパスワードが正しいユーザーのみ到達
        
        val session = request.device_id?.let {
            userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(it, LocalDateTime.now())
        }
        // セッションが有効で、かつ現在のユーザーとIDが一致するか
        val isKnownDevice = session != null && session.userId == user!!.userId

        if (!isKnownDevice) {
            // 未知のデバイスからのログイン -> MFAチェックへ
            // パスワードは合っているので、ここで初めてMFAフローなどの分岐を行う
            
            // 最適化: Userエンティティのフラグを確認
            if (user!!.isMfaEnabled) {
                if (user.preferredMfaType == "TOTP") {
                    return LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfa_token = generateMfaToken(user.userId),
                        mfa_type = "TOTP"
                    )
                } else if (user.preferredMfaType == "EMAIL") {
                    // Email MFAの場合、ここでメール送信
                    emailVerificationService.sendVerificationEmail(user.userId, user.email!!)
                    
                    return LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfa_token = generateMfaToken(user.userId),
                        mfa_type = "EMAIL"
                    )
                }
            }

            // MFAが無効な場合でも、新しいデバイスからのログイン通知などはここで行う（必要であれば）
            // 現状はメール認証フローへ
            val flowId = emailVerificationService.sendVerificationEmail(user.userId, user.email ?: user.username)
            val maskedEmail = user.email?.let { maskEmail(it) }
            
            return LoginResponse(
                status = "VERIFICATION_REQUIRED",
                user = null,
                require_verification = true,
                flow_id = flowId,
                masked_email = maskedEmail,
                mfa_token = null
            )
        }

        // ここまで来たら、パスワード認証OK かつ 既知のデバイス
        // MFAチェック
        if (user!!.isMfaEnabled) {
            if (user.preferredMfaType == "TOTP") {
                    return LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfa_token = generateMfaToken(user.userId),
                        mfa_type = "TOTP"
                    )
                } else if (user.preferredMfaType == "EMAIL") {
                 emailVerificationService.sendVerificationEmail(user.userId, user.email!!)
                 return LoginResponse(
                    status = "MFA_REQUIRED",
                    user = null,
                    mfa_token = generateMfaToken(user.userId),
                    mfa_type = "EMAIL"
                )
            }
        }

        // ログイン成功処理
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        val sessionKey = if (session != null) {
            session.lastAccessedAt = LocalDateTime.now()
            userSessionRepository.save(session)
            session.sessionKey
        } else {
            // 新規セッション作成
            createSession(user.userId, ipAddress, userAgent) // userAgentをdeviceNameとして使用
        }

        val userProfile = userProfileRepository.findById(user.userId).orElse(null)
        
        return LoginResponse(
            status = "AUTHENTICATED",
            user = UserInfo(
                username = user.username,
                display_name = user.displayName,
                email = user.email,
                avatar_url = userProfile?.profileImageUrl,
                is_email_verified = user.emailVerified.toInt() == 1
            ),
            flow_id = sessionKey // セッションキーを返す
        )
    }

    /**
     * MFA認証検証とログイン完了
     */
    @Transactional
    fun verifyMfa(mfaToken: String, code: String, ipAddress: String? = null, deviceName: String? = null): LoginResponse {
        val userId = validateMfaToken(mfaToken) ?: throw IllegalArgumentException("Invalid or expired MFA token")
        val user = userRepository.findById(userId).orElseThrow { IllegalStateException("User not found") }

        var isValid = false
        if (user.preferredMfaType == "EMAIL") {
             isValid = emailVerificationService.verifyCodeForMfa(userId, code)
        } else {
             isValid = mfaService.verifyCode(userId, code) || mfaService.verifyBackupCode(userId, code)
        }

        if (!isValid) {
             throw IllegalArgumentException("Invalid verification code")
        }

        // ログイン成功と同様の処理
        // ログイン成功と同様の処理
        
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        val sessionKey = createSession(userId, ipAddress, deviceName)
        
        val userProfile = userProfileRepository.findById(userId).orElse(null)

        return LoginResponse(
            status = "AUTHENTICATED",
            user = UserInfo(
                username = user.username,
                display_name = user.displayName,
                email = user.email,
                avatar_url = userProfile?.profileImageUrl,
                is_email_verified = user.emailVerified.toInt() == 1
            ),
            flow_id = sessionKey // セッションキーをflow_idとして返す（コントローラーでCookieにセットする）
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
                username = savedUser.username,
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
    @Transactional
    fun enableEmailMfa(userId: Int) {
        val user = userRepository.findById(userId).orElseThrow { IllegalStateException("User not found") }
        user.isMfaEnabled = true
        user.preferredMfaType = "EMAIL"
        userRepository.save(user)
    }
}
