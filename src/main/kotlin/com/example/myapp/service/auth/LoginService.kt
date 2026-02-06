package com.example.myapp.service.auth

import com.example.myapp.dto.auth.LoginRequest
import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.UserInfo
import com.example.myapp.entity.user.User
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.user.UserEmailRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.util.AuthUtils
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.service.user.profile.UserInfoBuilderService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository,
    private val userSessionRepository: UserSessionRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val emailVerificationService: EmailVerificationService,
    private val mfaService: MfaService,
    private val sessionService: SessionService,
    private val userInfoBuilderService: UserInfoBuilderService,
    private val dataMergeService: DataMergeService
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    // ... (keep private helper methods same) ...

    /**
     * ユーザーのプライマリメールアドレスを取得
     */
    private fun getPrimaryEmail(userId: Int): String? {
        return userEmailRepository.findByUserIdAndIsPrimaryTrue(userId)?.email
    }

    /**
     * メールアドレスでユーザーIDを検索
     */
    private fun findUserIdByEmail(email: String): Int? {
        return userEmailRepository.findByEmail(email)?.userId
    }
    
    fun getUserById(userId: Int): User? {
        return userRepository.findById(userId).orElse(null)
    }

    fun getUserByIdentifier(identifier: String): User? {
        // Try username first
        var user = userRepository.findByUsername(identifier)
        if (user == null) {
            // Try email
            val emailRecord = userEmailRepository.findByEmail(identifier)
            if (emailRecord != null) {
                user = userRepository.findById(emailRecord.userId).orElse(null)
            }
        }
        return user
    }

    /**
     * ログイン処理
     */
    @Transactional
    fun login(request: LoginRequest, ipAddress: String? = null, userAgent: String? = null, guestId: String? = null): LoginResponse {
        // ユーザー名またはメールでユーザーを検索
        var user = userRepository.findByUsername(request.username)
        if (user == null) {
            // メールで検索
            val userId = findUserIdByEmail(request.username)
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null)
            }
        }
        
        if (request.password == null) {
            return LoginResponse(
                status = "PASSWORD_REQUIRED",
                user = null
            )
        }

        // パスワードチェック
        val isPasswordCorrect = user != null && passwordEncoder.matches(request.password, user.passwordHash)
        if (!isPasswordCorrect) {
             return LoginResponse(
                status = "INVALID_CREDENTIALS",
                user = null,
                message = "ユーザー名またはパスワードが間違っています"
            )
        }
        
        // ここから先はパスワードが正しいユーザーのみ到達
        
        val session = request.device_id?.let {
            sessionService.getValidSession(it)
        }
        // セッションが有効で、かつ現在のユーザーとIDが一致するか
        val isKnownDevice = session != null && session.userId == user!!.userId

        if (!isKnownDevice) {
            // 未知のデバイスからのログイン -> MFAチェックへ
            
            // UserAuthStatusエンティティのフラグを確認
            val authStatus = userAuthStatusRepository.findByUserId(user!!.userId)
            if (authStatus != null && authStatus.isMfaEnabled) {
                if (authStatus.primaryMfaType == "TOTP") {
                    val email = getPrimaryEmail(user.userId)
                    return LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfa_token = mfaService.generateLoginMfaToken(user.userId),
                        mfa_type = "TOTP",
                        masked_email = email?.let { AuthUtils.maskEmail(it) }
                    )
                } else if (authStatus.primaryMfaType == "EMAIL") {
                    // Email MFAの場合、ここでメール送信
                    val email = getPrimaryEmail(user.userId) ?: throw RuntimeException("メールアドレスが登録されていません")
                    emailVerificationService.sendVerificationEmail(user.userId, email)
                    
                    return LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfa_token = mfaService.generateLoginMfaToken(user.userId),
                        mfa_type = "EMAIL",
                        masked_email = AuthUtils.maskEmail(email)
                    )
                }
            }

            // MFAが無効な場合でも、新しいデバイスからのログイン通知などはここで行う（必要であれば）
            // 現状はメール認証フローへ
            val email = getPrimaryEmail(user.userId) ?: user.username
            val flowId = emailVerificationService.sendVerificationEmail(user.userId, email).first
            val maskedEmail = getPrimaryEmail(user.userId)?.let { AuthUtils.maskEmail(it) }
            
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
        val authStatusForKnownDevice = userAuthStatusRepository.findByUserId(user!!.userId)
        if (authStatusForKnownDevice != null && authStatusForKnownDevice.isMfaEnabled) {
            if (authStatusForKnownDevice.primaryMfaType == "TOTP") {
                    val email = getPrimaryEmail(user.userId)
                    return LoginResponse(
                        status = "MFA_REQUIRED",
                        user = null,
                        mfa_token = mfaService.generateLoginMfaToken(user.userId),
                        mfa_type = "TOTP",
                        masked_email = email?.let { AuthUtils.maskEmail(it) }
                    )
                } else if (authStatusForKnownDevice.primaryMfaType == "EMAIL") {
                 val email = getPrimaryEmail(user.userId) ?: throw RuntimeException("メールアドレスが登録されていません")
                 emailVerificationService.sendVerificationEmail(user.userId, email)
                 return LoginResponse(
                    status = "MFA_REQUIRED",
                    user = null,
                    mfa_token = mfaService.generateLoginMfaToken(user.userId),
                    mfa_type = "EMAIL",
                    masked_email = AuthUtils.maskEmail(email)
                )
            }
        }

        // ログイン成功処理
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        val sessionKey = if (session != null) {
            sessionService.updateLastAccessed(session)
        } else {
            // 新規セッション作成
            sessionService.createSession(user.userId, ipAddress, userAgent)
        }

        // ゲストデータの統合
        if (guestId != null) {
            dataMergeService.mergeGuestData(user.userId, guestId)
        }

        val userInfo = userInfoBuilderService.buildUserInfo(user.userId)
        
        return LoginResponse(
            status = "AUTHENTICATED",
            user = userInfo,
            flow_id = sessionKey
        )
    }

    /**
     * MFA認証後などのログイン完了処理
     */
    @Transactional
    fun completeLogin(userId: Int, ipAddress: String? = null, userAgent: String? = null, guestId: String? = null): LoginResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalStateException("User not found") }
        
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        val sessionKey = sessionService.createSession(userId, ipAddress, userAgent)

        // ゲストデータの統合
        if (guestId != null) {
            dataMergeService.mergeGuestData(userId, guestId)
        }

        val userInfo = userInfoBuilderService.buildUserInfo(userId)

        return LoginResponse(
            status = "AUTHENTICATED",
            user = userInfo,
            flow_id = sessionKey
        )
    }
}
