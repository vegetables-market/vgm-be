package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.dto.auth.OAuthLoginRequest
import com.example.myapp.repository.user.UserProfileRepository
import com.example.myapp.service.auth.OAuthService
import com.example.myapp.service.auth.SessionService
import com.example.myapp.service.user.profile.UserInfoBuilderService
import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth/login")
class OAuthController(
    private val oauthService: OAuthService,
    private val sessionService: SessionService,
    private val userInfoBuilderService: UserInfoBuilderService,
    private val userProfileRepository: UserProfileRepository
) {

    @PostMapping("/google")
    @Transactional
    fun loginWithGoogle(
        @RequestBody request: OAuthLoginRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Any> {
        return verifyAndLogin(request.token, "google", servletRequest, servletResponse)
    }

    // 必要に応じて Microsoft, GitHub も追加

    private fun verifyAndLogin(
        idToken: String,
        provider: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        try {
            // 1. Verify Firebase Token
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            val uid = decodedToken.uid
            val email = decodedToken.email
            val name = decodedToken.name ?: "User"
            val picture = decodedToken.picture

            if (email == null) {
                return ResponseEntity.badRequest().body(mapOf("error" to "Email not found in token"))
            }

            // 2. Process OAuth User via OAuthService
            // This handles user creation, linking, and session creation
            val sessionKey = oauthService.processOAuth2User(email, name, provider, uid)

            // 3. Get Created Session & User ID
            val session = sessionService.getValidSession(sessionKey)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to create session"))
            
            val userId = session.userId

            // 4. Set Cookie
            val cookie = Cookie("vgm_session", sessionKey)
            cookie.isHttpOnly = true
            cookie.secure = request.isSecure // HTTPS only in production
            cookie.path = "/"
            cookie.maxAge = 30 * 24 * 60 * 60 // 30 days
            response.addCookie(cookie)

            // 5. Return Response
            val userProfile = userProfileRepository.findById(userId).orElse(null)
            
            // Build UserInfo
            val userInfo = userInfoBuilderService.buildUserInfo(userId)

            val loginResponse = LoginResponse(
                status = "AUTHENTICATED",
                user = userInfo,
                flow_id = sessionKey
            )

            return ResponseEntity.ok(loginResponse)

        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid token: ${e.message}"))
        }
    }
}
