package com.example.myapp.service.auth.session

import com.example.myapp.entity.auth.UserSession
import com.example.myapp.repository.auth.UserSessionRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SessionService(
    private val userSessionRepository: UserSessionRepository
) {
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    @Transactional
    fun createSession(userId: Int, ipAddress: String? = null, deviceName: String? = null, provider: String? = null): String {
        val sessionKey = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()
        
        val session = UserSession(
            userId = userId,
            sessionKey = sessionKey,
            refreshTokenHash = passwordEncoder.encode(refreshToken),
            deviceName = deviceName,
            ipAddress = ipAddress,
            provider = provider,
            expiresAt = LocalDateTime.now().plusDays(30)
        )
        
        userSessionRepository.save(session)
        return sessionKey
    }

    @Transactional
    fun logout(sessionKey: String) {
        val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(sessionKey, LocalDateTime.now())
        session?.let {
            it.isRevoked = true
            userSessionRepository.save(it)
        }
    }

    fun getValidSession(sessionKey: String): UserSession? {
        return userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
            sessionKey,
            LocalDateTime.now()
        )
    }

    /**
     * セッションの最終アクセス日時を更新
     */
    @Transactional
    fun updateLastAccessed(session: UserSession): String {
        session.lastAccessedAt = LocalDateTime.now()
        userSessionRepository.save(session)
        return session.sessionKey
    }
}
