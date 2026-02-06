package com.example.myapp.service.auth

import com.example.myapp.dto.user.mfa.*
import com.example.myapp.service.auth.mfa.MfaConfigService
import com.example.myapp.service.auth.mfa.MfaLoginService
import com.example.myapp.service.auth.mfa.TotpService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * MFA サービスの Facade
 * 外部に統一されたインターフェースを提供し、内部では各専門サービスに委譲
 */
@Service
class MfaService(
    private val totpService: TotpService,
    private val mfaConfigService: MfaConfigService,
    private val mfaLoginService: MfaLoginService
) {

    /**
     * MFA有効化を開始し、シークレットキーとQRコードURLを生成
     */
    fun startMfaSetup(userId: Int, userEmail: String): MfaSetupResponse {
        return totpService.generateQrCode(userId, userEmail)
    }

    /**
     * TOTPコードを検証してMFAを有効化
     */
    @Transactional
    fun verifyAndEnable(userId: Int, code: String): MfaEnableResponse {
        // TOTPコードを検証
        val secretKey = totpService.getSecretKey(userId)
            ?: throw IllegalStateException("MFA setup not started")

        if (!totpService.verifyTotpCode(secretKey, code)) {
            throw IllegalArgumentException("Invalid verification code")
        }

        // バックアップコードを生成
        val backupCodes = totpService.generateBackupCodes()

        // MFA を有効化
        mfaConfigService.enableTotpMfa(userId, secretKey, backupCodes)

        return MfaEnableResponse(
            success = true,
            backupCodes = backupCodes
        )
    }

    /**
     * Email MFAを有効化
     */
    @Transactional
    fun enableEmailMfa(userId: Int) {
        mfaConfigService.enableEmailMfa(userId)
    }

    /**
     * MFAを無効化
     */
    @Transactional
    fun disableMfa(userId: Int, code: String, password: String): Boolean {
        // TOTPコードを検証
        val secretKey = totpService.getSecretKey(userId)
            ?: throw IllegalStateException("MFA not configured")

        if (!totpService.verifyTotpCode(secretKey, code)) {
            throw IllegalArgumentException("Invalid verification code")
        }

        // パスワード検証は呼び出し元で実施されている前提

        // MFAを無効化
        mfaConfigService.disableMfa(userId)

        return true
    }

    /**
     * バックアップコードを再生成
     */
    @Transactional
    fun regenerateBackupCodes(userId: Int, password: String): List<String> {
        // パスワード検証は呼び出し元で実施

        return mfaConfigService.regenerateBackupCodes(userId)
    }

    /**
     * MFA状態を取得
     */
    fun getMfaStatus(userId: Int): MfaStatusResponse {
        return mfaConfigService.getMfaStatus(userId)
    }

    /**
     * TOTPコードを検証（ログイン時などに使用）
     */
    fun verifyCode(userId: Int, code: String): Boolean {
        return totpService.verifyUserTotpCode(userId, code)
    }

    /**
     * バックアップコードを検証
     */
    fun verifyBackupCode(userId: Int, code: String): Boolean {
        return totpService.verifyBackupCode(userId, code)
    }

    /**
     * ログインフローでのMFAトークン検証とコード検証
     * @return 検証成功したユーザーID
     */
    fun verifyLoginMfa(mfaToken: String, code: String): Int {
        return mfaLoginService.verifyLoginMfa(mfaToken, code)
    }

    /**
     * MFAトークンを生成
     * format: Base64(userId:expiry:signature)
     */
    fun generateLoginMfaToken(userId: Int): String {
        return mfaLoginService.generateLoginMfaToken(userId)
    }

    /**
     * MFAトークンを検証
     */
    fun validateLoginMfaToken(token: String): Int? {
        return mfaLoginService.validateLoginMfaToken(token)
    }

    /**
     * TOTPが有効かどうかを判定
     */
    fun isTotpEnabled(userId: Int): Boolean {
        return mfaConfigService.isTotpEnabled(userId)
    }
}
