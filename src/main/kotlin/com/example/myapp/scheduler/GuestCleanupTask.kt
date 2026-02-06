package com.example.myapp.scheduler

import com.example.myapp.repository.auth.GuestSessionRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class GuestCleanupTask(
    private val guestSessionRepository: GuestSessionRepository
) {

    /**
     * 毎日AM4時に期限切れのゲストセッションを削除
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    fun cleanupExpiredSessions() {
        val now = LocalDateTime.now()
        guestSessionRepository.deleteByExpiresAtBefore(now)
    }
}
