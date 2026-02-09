package com.example.myapp.repository.auth

import com.example.myapp.entity.auth.GuestSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface GuestSessionRepository : JpaRepository<GuestSession, String> {
    fun findByGuestIdAndExpiresAtAfter(guestId: String, now: LocalDateTime): GuestSession?
    fun deleteByExpiresAtBefore(now: LocalDateTime)
}
