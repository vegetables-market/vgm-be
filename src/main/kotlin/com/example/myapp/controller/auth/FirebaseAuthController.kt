package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.LoginResponse
import com.example.myapp.service.auth.LoginService
import com.example.myapp.service.auth.OAuthService
import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class FirebaseLoginRequest(
    val token: String
)

@RestController
@RequestMapping("/v1/auth")
class FirebaseAuthController(
    private val oauthService: OAuthService,
    private val loginService: LoginService
) {

    @PostMapping("/login/google")
    fun googleLogin(@RequestBody request: FirebaseLoginRequest, servletRequest: jakarta.servlet.http.HttpServletRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return processFirebaseLogin(request, servletRequest, response, "google")
    }

    @PostMapping("/login/microsoft")
    fun microsoftLogin(@RequestBody request: FirebaseLoginRequest, servletRequest: jakarta.servlet.http.HttpServletRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return processFirebaseLogin(request, servletRequest, response, "microsoft")
    }

    @PostMapping("/login/github")
    fun githubLogin(@RequestBody request: FirebaseLoginRequest, servletRequest: jakarta.servlet.http.HttpServletRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return processFirebaseLogin(request, servletRequest, response, "github")
    }


    /**
     * Firebase OAuth 共通処理
     */
    private fun processFirebaseLogin(
        request: FirebaseLoginRequest,
        servletRequest: jakarta.servlet.http.HttpServletRequest,
        response: HttpServletResponse,
        provider: String
    ): ResponseEntity<LoginResponse> {
        return try {
            // 1. Verify ID Token
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.token)
            val email = decodedToken.email
            val name = decodedToken.name ?: "No Name"

            if (email == null) {
                return ResponseEntity.badRequest().body(
                    LoginResponse(status = "ERROR", user = null, message = "Email not found in token")
                )
            }

            // 2. Process Login/Signup logic
            val providerUserId = decodedToken.uid  // Firebase UID
            val guestId = servletRequest.cookies?.find { it.name == com.example.myapp.service.auth.GuestSessionService.GUEST_COOKIE_NAME }?.value
            val sessionKey = oauthService.processOAuth2User(email, name, provider, providerUserId, guestId)

            // 3. Set Cookie
            val cookie = Cookie("vgm_session", sessionKey)
            cookie.path = "/"
            cookie.isHttpOnly = true
            cookie.maxAge = 60 * 60 * 24 * 30 // 30 days
            // cookie.secure = true // Production only
            response.addCookie(cookie)

            // 4. Return success response
            val user = loginService.getUserByIdentifier(email)
            
            ResponseEntity.ok(
                LoginResponse(
                    status = "AUTHENTICATED",
                    user = com.example.myapp.dto.auth.UserInfo(
                        username = user?.username ?: "",
                        display_name = user?.displayName ?: name,
                        email = email,
                        avatar_url = null,
                        is_email_verified = true
                    ),
                    flow_id = sessionKey
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(401).body(
                LoginResponse(status = "ERROR", user = null, message = "Invalid Token: ${e.message}")
            )
        }
    }
}
