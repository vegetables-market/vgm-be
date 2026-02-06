package com.example.myapp.service.auth.mfa

import com.example.myapp.dto.user.mfa.MfaStatusResponse
import com.example.myapp.repository.auth.TwoFactorAuthRepository
import com.example.myapp.repository.auth.UserAuthStatusRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * MFA設定管理を担当するサービス
 * 有効化・無効化・バックアップコード管理など
 */
@Service
class MfaConfigService(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
    private val userAuthStatusRepository: UserAuthStatusRepository,
    private val passwordEncoder: PasswordEncoder,
    private val totpService: TotpService
) {

    /**
     * TOTP MFA を有効化
     * @param userId ユーザーID
     * @param secretKey シークレットキー
     * @param backupCodes バックアップコード (平文)
     */
    @Transactional
    fun enableTotpMfa(userId: Int, secretKey: String, backupCodes: List<String>) {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow { IllegalStateException("MFA setup not started") }

        // バックアップコードをハッシュ化して保存
        val hashedCodes = backupCodes.map { passwordEncoder.encode(it) }
        twoFactorAuth.backupCodes = hashedCodes.joinToString(",")
        twoFactorAuth.secretKey = secretKey
        twoFactorAuth.isEnabled = true
        twoFactorAuth.updatedAt = LocalDateTime.now()

        twoFactorAuthRepository.save(twoFactorAuth)

        // UserAuthStatusテーブルのフラグを更新
        val authStatus = userAuthStatusRepository.findByUserId(userId)
            ?: throw IllegalStateException("Auth status not found")
        authStatus.isMfaEnabled = true
        authStatus.primaryMfaType = "TOTP"
        userAuthStatusRepository.save(authStatus)
    }

    /**
     * Email MFA を有効化
     * @param userId ユーザーID
     */
    @Transactional
    fun enableEmailMfa(userId: Int) {
        val authStatus = userAuthStatusRepository.findByUserId(userId)
            ?: throw IllegalStateException("Auth status not found")
        authStatus.isMfaEnabled = true
        authStatus.primaryMfaType = "EMAIL"
        userAuthStatusRepository.save(authStatus)
    }

    /**
     * MFA を無効化
     * @param userId ユーザーID
     */
    @Transactional
    fun disableMfa(userId: Int) {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow { IllegalStateException("MFA not configured") }

        // MFAを無効化（レコードは削除せず、フラグを変更）
        twoFactorAuth.isEnabled = false
        twoFactorAuth.updatedAt = LocalDateTime.now()
        twoFactorAuthRepository.save(twoFactorAuth)

        // UserAuthStatusテーブルのフラグを更新
        val authStatus = userAuthStatusRepository.findByUserId(userId)
            ?: throw IllegalStateException("Auth status not found")
        authStatus.isMfaEnabled = false
        authStatus.primaryMfaType = null
        userAuthStatusRepository.save(authStatus)
    }

    /**
     * バックアップコードを再生成
     * @param userId ユーザーID
     * @return 新しいバックアップコード (平文)
     */
    @Transactional
    fun regenerateBackupCodes(userId: Int): List<String> {
        val twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow { IllegalStateException("MFA not configured") }

        if (!twoFactorAuth.isEnabled) {
            throw IllegalStateException("MFA is not enabled")
        }

        val backupCodes = totpService.generateBackupCodes()
        val hashedCodes = backupCodes.map { passwordEncoder.encode(it) }

        twoFactorAuth.backupCodes = hashedCodes.joinToString(",")
        twoFactorAuth.updatedAt = LocalDateTime.now()

        twoFactorAuthRepository.save(twoFactorAuth)

        return backupCodes
    }

    /**
     * MFA状態を取得
     * @param userId ユーザーID
     * @return MFA状態
     */
    fun getMfaStatus(userId: Int): MfaStatusResponse {
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
     * プライマリMFAタイプを取得
     * @param userId ユーザーID
     * @return MFAタイプ (TOTP/EMAIL) or null
     */
    fun getPrimaryMfaType(userId: Int): String? {
        return userAuthStatusRepository.findByUserId(userId)?.primaryMfaType
    }

    /**
     * MFAが有効かどうかを判定
     * @param userId ユーザーID
     * @return 有効な場合true
     */
    fun isMfaEnabled(userId: Int): Boolean {
        val authStatus = userAuthStatusRepository.findByUserId(userId)
        return authStatus?.isMfaEnabled ?: false
    }

    /**
     * TOTPが有効かどうかを判定 (TwoFactorAuthテーブルを直接確認)
     * @param userId ユーザーID
     * @return 有効な場合true
     */
    fun isTotpEnabled(userId: Int): Boolean {
        return twoFactorAuthRepository.findByUserId(userId)
            .map { it.isEnabled }
            .orElse(false)
    }
}
