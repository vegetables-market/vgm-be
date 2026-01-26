package com.example.myapp.repository.auth

import com.example.myapp.entity.UserSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserSessionRepository : JpaRepository<UserSession, Long> {
    fun findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
        sessionKey: String,
        now: LocalDateTime
    ): UserSession?

    fun findByUserIdAndIsRevokedFalse(userId: Int): List<UserSession>
}
