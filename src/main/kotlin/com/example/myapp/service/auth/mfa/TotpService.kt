package com.example.myapp.service.auth.mfa

import com.example.myapp.dto.user.security.mfa.MfaSetupResponse
import com.example.myapp.entity.auth.TwoFactorAuth
import com.example.myapp.repository.auth.TwoFactorAuthRepository
import dev.samstevens.totp.code.*
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.secret.SecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import dev.samstevens.totp.time.TimeProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime

/**
 * TOTP (Time-based One-Time Password) の生成・検証を担当するサービス
 */
@Service
class TotpService(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
    private val secretGenerator: SecretGenerator,
    private val passwordEncoder: PasswordEncoder
) {
    private val timeProvider: TimeProvider = SystemTimeProvider()
    private val codeGenerator: CodeGenerator = DefaultCodeGenerator()
    private val codeVerifier: CodeVerifier = DefaultCodeVerifier(codeGenerator, timeProvider)

    /**
     * QRコードとシークレットキーを生成
     * @return MfaSetupResponse (secretKey, qrCodeUrl)
     */
    fun generateQrCode(userId: Int, userEmail: String): MfaSetupResponse {
        // 既にMFAが有効な場合はエラー
        val existing = twoFactorAuthRepository.findByUserId(userId)
        if (existing.isPresent && existing.get().isEnabled) {
            throw IllegalStateException("MFA is already enabled")
        }

        // シークレットキーを生成
        val secret = secretGenerator.generate()

        // 既存のレコードがあれば更新、なければ新規作成
        val twoFactorAuth = existing.orElse(
            TwoFactorAuth(
                userId = userId,
                secretKey = secret,
                isEnabled = false
            )
        ).apply {
            secretKey = secret
            isEnabled = false
            updatedAt = LocalDateTime.now()
        }

        twoFactorAuthRepository.save(twoFactorAuth)

        // QRコードデータを生成
        val qrData = QrData.Builder()
            .label(userEmail)
            .secret(secret)
            .issuer("VGM")
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build()

        val qrCodeUrl = qrData.uri

        return MfaSetupResponse(
            secret = secret,
            qrCodeUrl = qrCodeUrl
        )
    }

    /**
     * TOTPコードを検証
     * @param secretKey ユーザーのシークレットキー
     * @param code 検証するコード
     * @return 検証結果
     */
    fun verifyTotpCode(secretKey: String, code: String): Boolean {
        return codeVerifier.isValidCode(secretKey, code)
    }

    /**
     * ユーザーのTOTPコードを検証（ログイン時などに使用）
     * @param userId ユーザーID
     * @param code 検証するコード
     * @return 検証結果
     */
    fun verifyUserTotpCode(userId: Int, code: String): Boolean {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElse(null) ?: return false

        if (!twoFactorAuth.isEnabled) {
            return false
        }

        return codeVerifier.isValidCode(twoFactorAuth.secretKey, code)
    }

    /**
     * バックアップコードを生成
     * @return 10個のバックアップコード
     */
    fun generateBackupCodes(): List<String> {
        val random = SecureRandom()
        return (1..10).map {
            "%08d".format(random.nextInt(100000000))
        }
    }

    /**
     * バックアップコードを検証
     * @param userId ユーザーID
     * @param code 検証するバックアップコード
     * @return 検証結果
     */
    fun verifyBackupCode(userId: Int, code: String): Boolean {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElse(null) ?: return false

        if (!twoFactorAuth.isEnabled || twoFactorAuth.backupCodes.isNullOrEmpty()) {
            return false
        }

        val hashedCodes = twoFactorAuth.backupCodes!!.split(",")

        return hashedCodes.any { passwordEncoder.matches(code, it) }
    }

    /**
     * ユーザーのシークレットキーを取得
     * @param userId ユーザーID
     * @return シークレットキー (存在しない場合は null)
     */
    fun getSecretKey(userId: Int): String? {
        return twoFactorAuthRepository.findByUserId(userId)
            .map { it.secretKey }
            .orElse(null)
    }
}
