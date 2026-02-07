package com.example.myapp.service.auth

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.UserInfo
import com.example.myapp.service.auth.LoginService
import com.example.myapp.service.auth.OAuthService
import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Service

@Service
class FirebaseAuthService(
    private val oauthService: OAuthService,
    private val loginService: LoginService
) {

    /**
     * Firebase OAuth 共通処理
     */
    /**
     * Firebase OAuth 共通処理
     */
    fun processLogin(
        token: String,
        provider: String,
        guestId: String?
    ): LoginResponse {
        try {
            // 1. Verify ID Token
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
            val email = decodedToken.email
            val name = decodedToken.name ?: "No Name"

            if (email == null) {
                throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.INVALID_INPUT, "Email not found in token")
            }

            // 2. Process Login/Signup logic
            val providerUserId = decodedToken.uid  // Firebase UID
            val sessionKey = oauthService.processOAuth2User(email, name, provider, providerUserId, guestId)

            // 3. Return success response
            val user = loginService.getUserByIdentifier(email)
            
            return LoginResponse(
                status = "AUTHENTICATED",
                user = UserInfo(
                    username = user?.username ?: "",
                    display_name = user?.displayName ?: name,
                    email = email,
                    avatar_url = null,
                    is_email_verified = true
                ),
                flow_id = sessionKey
            )

        } catch (e: com.example.myapp.exception.AppException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FIREBASE_ERROR, e.message ?: "Unknown Firebase Error")
        }
    }
}
