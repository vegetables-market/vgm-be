package com.example.myapp.service

import com.example.myapp.dto.user.*
import com.example.myapp.entity.auth.TwoFactorAuth
import com.example.myapp.repository.auth.TwoFactorAuthRepository
import dev.samstevens.totp.code.*
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.QrDataFactory
import dev.samstevens.totp.secret.SecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import dev.samstevens.totp.time.TimeProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class MfaService(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
    private val userRepository: com.example.myapp.repository.user.UserRepository,
    private val secretGenerator: SecretGenerator,
    private val passwordEncoder: PasswordEncoder
) {
    private val timeProvider: TimeProvider = SystemTimeProvider()
    private val codeGenerator: CodeGenerator = DefaultCodeGenerator()
    private val codeVerifier: CodeVerifier = DefaultCodeVerifier(codeGenerator, timeProvider)

    /**
     * MFA有効化を開始し、シークレットキーとQRコードURLを生成
     */
    fun startMfaSetup(userId: Int, userEmail: String): MfaSetupResponse {
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
     * TOTPコードを検証してMFAを有効化
     */
    @Transactional
    fun verifyAndEnable(userId: Int, code: String): MfaEnableResponse {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow { IllegalStateException("MFA setup not started") }

        // TOTPコードを検証
        if (!codeVerifier.isValidCode(twoFactorAuth.secretKey, code)) {
            throw IllegalArgumentException("Invalid verification code")
        }

        // バックアップコードを生成
        val backupCodes = generateBackupCodes()

        // バックアップコードをハッシュ化して保存
        val hashedCodes = backupCodes.map { passwordEncoder.encode(it) }
        twoFactorAuth.backupCodes = hashedCodes.joinToString(",")
        twoFactorAuth.isEnabled = true
        twoFactorAuth.updatedAt = LocalDateTime.now()

        twoFactorAuthRepository.save(twoFactorAuth)

        // ユーザーテーブルのフラグも更新
        val user = userRepository.findById(userId).orElseThrow { IllegalStateException("User not found") }
        user.isMfaEnabled = true
        user.preferredMfaType = "TOTP" // Set preferred MFA type
        userRepository.save(user)

        return MfaEnableResponse(
            success = true,
            backupCodes = backupCodes
        )
    }

    /**
     * MFAを無効化
     */
    @Transactional
    fun disableMfa(userId: Int, code: String, password: String): Boolean {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow { IllegalStateException("MFA not configured") }

        // TOTPコードを検証
        if (!codeVerifier.isValidCode(twoFactorAuth.secretKey, code)) {
            throw IllegalArgumentException("Invalid verification code")
        }

        // パスワード検証は呼び出し元で実施されている前提

        // MFAを無効化（レコードは削除せず、フラグを変更）
        twoFactorAuth.isEnabled = false
        twoFactorAuth.updatedAt = LocalDateTime.now()
        twoFactorAuthRepository.save(twoFactorAuth)

        // ユーザーテーブルのフラグも更新
        val user = userRepository.findById(userId).orElseThrow { IllegalStateException("User not found") }
        user.isMfaEnabled = false
        user.preferredMfaType = null // Clear preferred MFA type
        userRepository.save(user)

        return true
    }

    /**
     * バックアップコードを再生成
     */
    @Transactional
    fun regenerateBackupCodes(userId: Int, password: String): List<String> {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow { IllegalStateException("MFA not configured") }

        if (!twoFactorAuth.isEnabled) {
            throw IllegalStateException("MFA is not enabled")
        }

        // パスワード検証は呼び出し元で実施

        val backupCodes = generateBackupCodes()
        val hashedCodes = backupCodes.map { passwordEncoder.encode(it) }

        twoFactorAuth.backupCodes = hashedCodes.joinToString(",")
        twoFactorAuth.updatedAt = LocalDateTime.now()

        twoFactorAuthRepository.save(twoFactorAuth)

        return backupCodes
    }

    /**
     * MFA状態を取得
     */
    fun getMfaStatus(userId: Int): MfaStatusResponse {
        // 最適化: Userテーブルから状態を取得（DBアクセス1回）
        // ただしTwoFactorAuthのcreatedAtが必要なので、現状はまだTwoFactorAuthを見る必要がある
        // もしcreatedAtをUIで表示しないならUserテーブルだけで済むが、今回は既存維持
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)

        return if (twoFactorAuth.isPresent) {
            MfaStatusResponse(
                isEnabled = twoFactorAuth.get().isEnabled,
                createdAt = twoFactorAuth.get().createdAt.toString()
            )
        } else {
            MfaStatusResponse(
                isEnabled = false,
                createdAt = null
            )
        }
    }

    /**
     * TOTPコードを検証（ログイン時などに使用）
     */
    fun verifyCode(userId: Int, code: String): Boolean {
        // 検証時はシークレットキーが必要なので t_user_two_factor を見る必要がある
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElse(null) ?: return false

        if (!twoFactorAuth.isEnabled) {
            // ここで不整合（UserテーブルはtrueだがTwoFactorテーブルはfalse）の場合がありうるが
            // 基本はTwoFactorAuthが正
            return false
        }

        return codeVerifier.isValidCode(twoFactorAuth.secretKey, code)
    }

    /**
     * バックアップコードを検証
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
     * バックアップコードを10個生成
     */
    private fun generateBackupCodes(): List<String> {
        val random = SecureRandom()
        return (1..10).map {
            "%08d".format(random.nextInt(100000000))
        }
    }
}
