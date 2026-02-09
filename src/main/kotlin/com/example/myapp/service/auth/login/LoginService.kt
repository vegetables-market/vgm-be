package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginRequest
import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.user.User
import com.example.myapp.service.auth.session.SessionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val loginValidationService: LoginValidationService,
    private val loginMfaService: LoginMfaService,
    private val loginCompletionService: LoginCompletionService,
    private val sessionService: SessionService
) {

    /**
     * ユーザーIDからユーザーを取得する (Helper for other services)
     */
    fun getUserById(userId: Int): User? {
        return loginValidationService.getUserById(userId)
    }

    /**
     * 識別子（ユーザー名またはメールアドレス）からユーザーを取得する (Helper for other services)
     */
    fun getUserByIdentifier(identifier: String): User? {
        return loginValidationService.getUserByIdentifier(identifier)
    }

    /**
     * ログイン処理
     * @param request ログインリクエスト
     * @param ipAddress IPアドレス
     * @param userAgent User-Agent
     * @param guestId ゲストID
     */
    @Transactional
    fun login(request: LoginRequest, ipAddress: String? = null, userAgent: String? = null, guestId: String? = null): LoginResponse {
        // 1. バリデーション & ユーザー特定
        val validationResult = loginValidationService.validateUser(request)
        
        val user = when (validationResult) {
            is LoginValidationService.ValidationResult.Success -> validationResult.user
            is LoginValidationService.ValidationResult.Error -> return validationResult.response
        }
        
        // 2. セッションチェック (既知のデバイスかどうか)
        val existingSession = request.deviceId?.let { sessionService.getValidSession(it) }
        val isKnownDevice = existingSession != null && existingSession.userId == user.userId

        // 3. MFAチェック & 未知のデバイスチェック
        val mfaResult = loginMfaService.checkMfaStep(user, isKnownDevice)
        if (mfaResult is LoginMfaService.MfaCheckResult.MfaRunning) {
            return mfaResult.response
        }

        // 4. ログイン完了処理
        return loginCompletionService.completeLogin(
            userId = user.userId,
            existingSession = existingSession,
            ipAddress = ipAddress,
            userAgent = userAgent,
            guestId = guestId
        )
    }

    /**
     * MFA認証後などのログイン完了処理
     */
    @Transactional
    fun completeLogin(userId: Int, ipAddress: String? = null, userAgent: String? = null, guestId: String? = null): LoginResponse {
        return loginCompletionService.completeLogin(
            userId = userId,
            ipAddress = ipAddress,
            userAgent = userAgent,
            guestId = guestId
        )
    }
}

