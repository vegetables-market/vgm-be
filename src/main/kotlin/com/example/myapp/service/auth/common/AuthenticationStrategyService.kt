package com.example.myapp.service.auth.common

import com.example.myapp.repository.auth.UserAuthStatusRepository
import com.example.myapp.service.auth.MfaService
import org.springframework.stereotype.Service

enum class AuthType {
    TOTP,
    EMAIL
}

/**
 * 認証戦略サービス
 * ユーザーにとって「最強」の認証方式を決定する
 */
@Service
class AuthenticationStrategyService(
    private val mfaService: MfaService,
    private val userAuthStatusRepository: UserAuthStatusRepository
) {
    /**
     * ユーザーに要求すべき認証方式を決定する
     * @param userId ユーザーID
     * @return AuthType (TOTP or EMAIL)
     */
    fun determineRequiredAuthType(userId: Int): AuthType {
        // 1. TOTPが有効なら、設定に関わらず最強のTOTPを優先
        if (mfaService.isTotpEnabled(userId)) {
            return AuthType.TOTP
        }

        // 2. TOTPが無効で、かつMFAとしてのEMAILが有効な場合はEMAIL
        // ただし、MFA未設定のユーザーでも、重要アクション時には「少なくともEmail認証」を行うべきなので、
        // デフォルトとして EMAIL を返す
        return AuthType.EMAIL
    }
}
