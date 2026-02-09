package com.example.myapp.controller.auth.verify

import com.example.myapp.dto.auth.verify.AuthMethod
import com.example.myapp.dto.auth.login.LoginResponse
import com.example.myapp.dto.auth.verify.VerifyAuthRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.verification.AuthCodeVerificationService
import com.example.myapp.service.auth.login.LoginService
import com.example.myapp.service.auth.MfaService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * ログイン検証・完了コントローラー
 * EMAIL/TOTP認証後にセッションを作成してログインを完了する
 */
@RestController
@RequestMapping("/v1/auth")
class LoginVerificationController(
    private val loginService: LoginService,
    private val mfaService: MfaService,
    private val authCodeVerificationService: AuthCodeVerificationService,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val appCookieService: AppCookieService
) {

    @PostMapping("/verify-login")
    fun verifyLogin(
        @RequestBody request: VerifyAuthRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        return try {
            // 認証コードを検証してユーザーIDを取得
            val userId = authCodeVerificationService.verifyAuthCode(request.method, request.identifier, request.code)
                ?: throw AppException(ErrorCode.AUTH_CODE_INVALID, "Invalid or expired code")

            // Email MFAを有効化（プライマリMFAタイプがnullの場合）
            if (request.method == AuthMethod.EMAIL) {
                val authStatus = userAuthStatusRepository.findByUserId(userId)
                if (authStatus?.primaryMfaType == null) {
                    mfaService.enableEmailMfa(userId)
                }
            }

            // ログイン完了処理（セッション作成）
            val ipAddress = servletRequest.remoteAddr
            val userAgent = servletRequest.getHeader("User-Agent")
            val response = loginService.completeLogin(userId, ipAddress, userAgent)

            // セッションCookieを設定
            if (response.flowId != null) {
                appCookieService.addSessionCookie(servletResponse, response.flowId)
            }

            ResponseEntity.ok(response)

        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException(ErrorCode.AUTH_CODE_INVALID, e.message ?: "認証に失敗しました")
        }
    }
}
