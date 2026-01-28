package com.example.myapp.controller

import com.example.myapp.dto.*
import com.example.myapp.service.AuthService
import com.example.myapp.service.TotpService
import com.example.myapp.service.EmailService
import com.example.myapp.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val totpService: TotpService,
    private val emailService: EmailService,
    private val userRepository: UserRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()
    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<RegisterResponse> {
        val response = authService.register(registerRequest)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(loginRequest)
        // requireTotpがtrueの場合はHTTP 200で返す（フロントエンドがレスポンスを受け取れるように）
        return if (response.success || response.requireTotp) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    @GetMapping("/user/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<LoginResponse> {
        val user = authService.getUserById(userId)
        return if (user != null) {
            ResponseEntity.ok(LoginResponse(
                success = true,
                message = "ユーザー情報を取得しました",
                userId = user.id,
                username = user.username,
                email = user.email,
                theme = user.theme
            ))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * TOTP有効化開始（シークレットキー生成＆メール送信）
     */
    @PostMapping("/totp/enable")
    fun enableTotp(@RequestBody request: EnableTotpRequest): ResponseEntity<EnableTotpResponse> {
        val user = userRepository.findById(request.userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(
                EnableTotpResponse(
                    success = false,
                    message = "ユーザーが見つかりません"
                )
            )

        if (user.totpEnabled) {
            return ResponseEntity.badRequest().body(
                EnableTotpResponse(
                    success = false,
                    message = "既にTOTPが有効化されています"
                )
            )
        }

        // シークレットキー生成
        val secret = totpService.generateSecret()
        user.totpSecret = secret
        userRepository.save(user)

        // QRコードURI生成
        val qrCodeUri = totpService.generateQrCodeUri(user.username, secret)

        // QRコード画像（Base64）生成
        val qrCodeImage = totpService.generateQrCodeImageBase64(user.username, secret)

        // メールでシークレットキーを送信
        try {
            emailService.sendSecretKeyEmail(user.email, user.username, secret)
        } catch (e: Exception) {
            println("メール送信エラー: ${e.message}")
        }

        return ResponseEntity.ok(
            EnableTotpResponse(
                success = true,
                message = "シークレットキーを生成しました。メールを確認してください。",
                secret = secret,
                qrCodeUri = qrCodeUri,
                qrCodeImage = qrCodeImage
            )
        )
    }

    /**
     * TOTP検証＆有効化完了
     */
    @PostMapping("/totp/verify-and-enable")
    fun verifyAndEnableTotp(@RequestBody request: VerifyAndEnableTotpRequest): ResponseEntity<VerifyAndEnableTotpResponse> {
        val user = userRepository.findById(request.userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(
                VerifyAndEnableTotpResponse(
                    success = false,
                    message = "ユーザーが見つかりません"
                )
            )

        if (user.totpSecret == null) {
            return ResponseEntity.badRequest().body(
                VerifyAndEnableTotpResponse(
                    success = false,
                    message = "まずTOTP有効化を開始してください"
                )
            )
        }

        // TOTPコード検証
        if (totpService.verifyCode(user.totpSecret!!, request.code)) {
            user.totpEnabled = true
            userRepository.save(user)

            return ResponseEntity.ok(
                VerifyAndEnableTotpResponse(
                    success = true,
                    message = "TOTP認証が有効化されました"
                )
            )
        } else {
            return ResponseEntity.badRequest().body(
                VerifyAndEnableTotpResponse(
                    success = false,
                    message = "コードが正しくありません"
                )
            )
        }
    }

    /**
     * TOTP無効化
     */
    @PostMapping("/totp/disable")
    fun disableTotp(@RequestBody request: DisableTotpRequest): ResponseEntity<DisableTotpResponse> {
        val user = userRepository.findById(request.userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(
                DisableTotpResponse(
                    success = false,
                    message = "ユーザーが見つかりません"
                )
            )

        // パスワード検証（本人確認）
        if (!passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.badRequest().body(
                DisableTotpResponse(
                    success = false,
                    message = "パスワードが正しくありません"
                )
            )
        }

        user.totpEnabled = false
        user.totpSecret = null
        userRepository.save(user)

        return ResponseEntity.ok(
            DisableTotpResponse(
                success = true,
                message = "TOTP認証を無効化しました"
            )
        )
    }

    /**
     * TOTPログイン
     */
    @PostMapping("/login/totp")
    fun loginWithTotp(@RequestBody request: TotpLoginRequest): ResponseEntity<LoginWithTotpResponse> {
        val user = userRepository.findByUsername(request.username)
            ?: return ResponseEntity.badRequest().body(
                LoginWithTotpResponse(
                    success = false,
                    message = "ユーザー名またはパスワードが間違っています"
                )
            )

        // パスワード検証
        if (!passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.badRequest().body(
                LoginWithTotpResponse(
                    success = false,
                    message = "ユーザー名またはパスワードが間違っています"
                )
            )
        }

        // TOTP検証
        if (user.totpEnabled && user.totpSecret != null) {
            if (totpService.verifyCode(user.totpSecret!!, request.totpCode)) {
                return ResponseEntity.ok(
                    LoginWithTotpResponse(
                        success = true,
                        message = "ログインに成功しました",
                        userId = user.id,
                        username = user.username,
                        email = user.email
                    )
                )
            } else {
                return ResponseEntity.badRequest().body(
                    LoginWithTotpResponse(
                        success = false,
                        message = "TOTPコードが正しくありません"
                    )
                )
            }
        }

        return ResponseEntity.badRequest().body(
            LoginWithTotpResponse(
                success = false,
                message = "TOTP認証が有効化されていません"
            )
        )
    }
}
