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
        guestId: String?
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
            val providerUserId = decodedToken.uid  // Firebase UID
            val sessionKey = oauthService.processOAuth2User(email, name, provider, providerUserId, guestId)

            // 3. 成功レスポンスを返す
            var user = userRepository.findByUsername(email)
            if (user == null) {
                user = userEmailRepository.findByEmail(email)?.let { 
                     userRepository.findById(it.userId).orElse(null) 
                }
            }
            
            return LoginResponse(
                status = "AUTHENTICATED",
                user = UserInfo(
                    username = user?.username ?: "",
                    displayName = user?.displayName ?: name,
                    email = email,
                    avatarUrl = null, // TODO: ユーザーエンティティまたはトークンから取得
                    isEmailVerified = true
                ),
                flowId = sessionKey
            )

        } catch (e: com.example.myapp.exception.AppException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FIREBASE_ERROR, e.message ?: "Unknown Firebase Error")
        }
    }
}
