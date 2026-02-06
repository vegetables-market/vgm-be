package com.example.myapp.controller.auth

import com.example.myapp.dto.auth.AuthMethod
import com.example.myapp.dto.auth.VerifyAuthRequest
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.LoginService
import com.example.myapp.service.auth.MfaService
import com.example.myapp.service.auth.SensitiveActionService
import com.example.myapp.service.email.EmailVerificationService
import com.example.myapp.repository.auth.UserAuthStatusRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class VerificationController(
    private val loginService: LoginService,
    private val mfaService: MfaService,
    private val emailVerificationService: EmailVerificationService,
    private val sensitiveActionService: SensitiveActionService,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {

    /**
     * ログイン完了エンドポイント
     * EMAIL/TOTP認証後にセッションを作成してログインを完了する
     */
    @PostMapping("/verify-login")
    fun verifyLogin(
        @RequestBody request: VerifyAuthRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            // 認証コードを検証してユーザーIDを取得
            val userId = verifyAuthCode(request.method, request.identifier, request.code)
                ?: return ResponseEntity.badRequest().body(
                    mapOf("success" to false, "message" to "Invalid or expired code")
                )

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
            val sessionKey = response.flow_id
            if (sessionKey != null) {
                val cookie = Cookie("vgm_session", sessionKey)
                cookie.isHttpOnly = true
                cookie.maxAge = 30 * 24 * 60 * 60 // 30 days
                cookie.path = "/"
                servletResponse.addCookie(cookie)
            }

            // レスポンス
            ResponseEntity.ok(mapOf(
                "success" to true,
                "status" to response.status,
                "user" to response.user,
                "flow_id" to response.flow_id,
                "mfa_token" to response.mfa_token,
                "mfa_type" to response.mfa_type,
                "masked_email" to response.masked_email,
                "require_verification" to response.require_verification
            ))

        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException(ErrorCode.AUTH_CODE_INVALID, e.message ?: "認証に失敗しました")
        }
    }

    /**
     * セキュリティ確認エンドポイント
     * 重要アクション実行前の再認証でアクショントークンを発行する
     */
    @PostMapping("/verify-action")
    fun verifyAction(
        @RequestBody request: VerifyAuthRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            // actionパラメータは必須
            if (request.action == null) {
                throw AppException(ErrorCode.INVALID_INPUT, "Action parameter is required")
            }

            // 認証コードを検証してユーザーIDを取得
            val userId = verifyAuthCode(request.method, request.identifier, request.code)
                ?: return ResponseEntity.badRequest().body(
                    mapOf("success" to false, "message" to "Invalid or expired code")
                )

            // アクショントークンを発行
            val actionToken = sensitiveActionService.issueActionToken(userId, request.action)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "action_token" to actionToken,
                "user" to mapOf("user_id" to userId),
                "action" to request.action
            ))

        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException(ErrorCode.AUTH_CODE_INVALID, e.message ?: "認証に失敗しました")
        }
    }

    /**
     * 共通の認証コード検証ロジック
     * EMAIL/TOTPの認証コードを検証してユーザーIDを返す
     */
    private fun verifyAuthCode(method: AuthMethod, identifier: String, code: String): Int? {
        return when (method) {
            AuthMethod.EMAIL -> verifyEmailCode(identifier, code)
            AuthMethod.TOTP -> verifyTotpCode(identifier, code)
        }
    }

    /**
     * Email認証コードの検証
     */
    private fun verifyEmailCode(flowId: String, code: String): Int? {
        val verification = emailVerificationService.verifyByFlowId(flowId, code) ?: return null
        return verification.userId ?: loginService.getUserByIdentifier(verification.email!!)?.userId
    }

    /**
     * TOTP認証コードの検証
     */
    private fun verifyTotpCode(mfaToken: String, code: String): Int? {
        return try {
            mfaService.verifyLoginMfa(mfaToken, code)
        } catch (e: Exception) {
            null
        }
    }
}
