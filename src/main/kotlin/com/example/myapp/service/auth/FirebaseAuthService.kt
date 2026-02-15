package com.example.myapp.service.auth

import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.dto.auth.login.UserInfo
import com.example.myapp.dto.auth.firebase.VerifiedToken
import com.google.firebase.auth.FirebaseAuth
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.email.UserEmailRepository
import org.springframework.stereotype.Service

@Service
class FirebaseAuthService(
    private val oauthService: OAuthService,
    private val userRepository: UserRepository,
    private val userEmailRepository: UserEmailRepository
) {

    /**
     * Firebase OAuth 共通処理
     */
    /**
     * Firebase OAuth 共通処理
     */
    /**
     * IDトークンを検証し、ユーザー情報を返す
     */
    fun verifyToken(token: String): VerifiedToken {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        val email = decodedToken.email
            ?: throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.INVALID_INPUT, "Email not found in token")
        
        return VerifiedToken(
            uid = decodedToken.uid,
            email = email,
            name = decodedToken.name ?: "No Name",
            picture = decodedToken.picture
        )
    }

    /**
     * Firebase OAuth 共通処理
     */
    fun processLogin(
        token: String,
        provider: String,
        guestId: String?,
        ipAddress: String? = null,
        userAgent: String? = null
    ): LoginResponse {
        try {
            // 1. IDトークンを検証
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
            val email = decodedToken.email
            val name = decodedToken.name ?: "No Name"

            if (email == null) {
                throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.INVALID_INPUT, "Email not found in token")
            }

            // 2. ログイン/登録ロジックを実行
            // 2. ログイン/登録ロジックを実行
            val providerUserId = decodedToken.uid  // Firebase UID
            val result = oauthService.processOAuth2User(email, name, provider, providerUserId, guestId, ipAddress, userAgent)

            // 3. 結果に応じたレスポンスを返す
            return when (result) {
                is com.example.myapp.service.auth.OAuthService.OAuthProcessingResult.Authenticated -> {
                    var user = userRepository.findByUsername(email)
                    if (user == null) {
                        user = userEmailRepository.findByEmail(email)?.let { 
                             userRepository.findById(it.userId).orElse(null) 
                        }
                    }
                    
                    LoginResponse(
                        status = "AUTHENTICATED",
                        user = UserInfo(
                            username = user?.username ?: "",
                            displayName = user?.displayName ?: name,
                            email = email,
                            avatarUrl = null,
                            isEmailVerified = true
                        ),
                        flowId = result.sessionKey
                    )
                }
                is com.example.myapp.service.auth.OAuthService.OAuthProcessingResult.NewUser -> {
                    // 新規ユーザー -> 登録フローへ誘導
                    LoginResponse(
                        status = "OAUTH_REGISTRATION_REQUIRED",
                        user = UserInfo(
                            username = "", // 未定
                            displayName = result.name,
                            email = result.email,
                            avatarUrl = null,
                            isEmailVerified = true
                        ),
                        oauthProvider = result.provider,
                        oauthToken = token // 検証済みのIDトークンを返す
                    )
                }
            }

        } catch (e: com.example.myapp.exception.AppException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FIREBASE_ERROR, e.message ?: "Unknown Firebase Error")
        }
    }
}
