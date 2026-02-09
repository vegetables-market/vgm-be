package com.example.myapp.service.auth.mfa

import com.example.myapp.service.email.verification.VerifyEmailCode
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class MfaLoginService(
    private val mfaConfigService: MfaConfigService,
    private val totpService: TotpService,
    private val verifyEmailCode: VerifyEmailCode
) {
    // TODO: 環境変数を使用する
    private val mfaTokenSecret = "vgm-mfa-token-secret-key-change-in-production-environment"

    /**
     * MFAトークンを生成
     * format: Base64(userId:expiry:signature)
     * @param userId ユーザーID
     * @return MFAトークン
     */
    fun generateLoginMfaToken(userId: Int): String {
        val expiry = System.currentTimeMillis() + 300000 // 5分
        val data = "$userId:$expiry"
        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(mfaTokenSecret.toByteArray(), "HmacSHA256")
        hmac.init(secretKey)
        val signature = Base64.getEncoder().encodeToString(hmac.doFinal(data.toByteArray()))
        return Base64.getEncoder().encodeToString("$data:$signature".toByteArray())
    }

    /**
     * MFAトークンを検証
     * @param token MFAトークン
     * @return ユーザーID (無効な場合null)
     */
    fun validateLoginMfaToken(token: String): Int? {
        try {
            val decoded = String(Base64.getDecoder().decode(token))
            val parts = decoded.split(":")
            if (parts.size != 3) return null

            val userId = parts[0].toInt()
            val expiry = parts[1].toLong()
            val signature = parts[2]

            if (System.currentTimeMillis() > expiry) return null

            val data = "$userId:$expiry"
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(mfaTokenSecret.toByteArray(), "HmacSHA256")
            hmac.init(secretKey)
            val expectedSignature = Base64.getEncoder().encodeToString(hmac.doFinal(data.toByteArray()))

            return if (signature == expectedSignature) userId else null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * ログインフローでのMFAトークン検証とコード検証
     * @param mfaToken MFAトークン
     * @param code 検証コード (TOTP/Email/Backup)
     * @return 検証成功したユーザーID
     * @throws IllegalArgumentException トークンまたはコードが無効な場合
     */
    fun verifyLoginMfa(mfaToken: String, code: String): Int {
        val userId = validateLoginMfaToken(mfaToken)
            ?: throw IllegalArgumentException("Invalid or expired MFA token")

        val primaryMfaType = mfaConfigService.getPrimaryMfaType(userId)

        val isValid = when (primaryMfaType) {
            "EMAIL" -> verifyEmailCode.verifyForMfa(userId, code)
            "TOTP" -> totpService.verifyUserTotpCode(userId, code) || totpService.verifyBackupCode(userId, code)
            else -> false
        }

        if (!isValid) {
            throw IllegalArgumentException("Invalid verification code")
        }

        return userId
    }
}
