package com.example.myapp.service.auth.login

import com.example.myapp.dto.auth.login.LoginRequest
import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.entity.user.User
import com.example.myapp.service.auth.session.SessionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ユーザーログインユースケース
 * ログインフロー全体（バリデーション -> MFAチェック -> 完了）を調整する
 */
@Service
class LoginUser(
    private val validateLoginRequest: ValidateLoginRequest,
    private val checkLoginMfa: CheckLoginMfa,
    private val completeLogin: CompleteLogin,
    private val sessionService: SessionService
) {

    /**
     * ログイン処理を実行する
     */
    @Transactional
    operator fun invoke(request: LoginRequest, ipAddress: String? = null, userAgent: String? = null, guestId: String? = null): LoginResponse {
        // 1. バリデーション & ユーザー特定
        val validationResult = validateLoginRequest(request)
        
        val user = when (validationResult) {
            is ValidateLoginRequest.Result.Success -> validationResult.user
            is ValidateLoginRequest.Result.Error -> return validationResult.response
        }
        
        // 2. セッションチェック (既知のデバイスかどうか)
        val existingSession = request.deviceId?.let { sessionService.getValidSession(it) }
        val isKnownDevice = existingSession != null && existingSession.userId == user.userId

        // 3. MFAチェック & 未知のデバイスチェック
        val mfaResult = checkLoginMfa(user, isKnownDevice)
        if (mfaResult is CheckLoginMfa.Result.MfaRunning) {
            return mfaResult.response
        }

        // 4. ログイン完了処理
        return completeLogin(
            userId = user.userId,
            existingSession = existingSession,
            ipAddress = ipAddress,
            userAgent = userAgent,
            guestId = guestId
        )
    }
}
