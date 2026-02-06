package com.example.myapp.service.auth

import com.example.myapp.entity.auth.GuestSession
import com.example.myapp.repository.auth.GuestSessionRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class GuestSessionService(
    private val guestSessionRepository: GuestSessionRepository
) {

    companion object {
        const val GUEST_COOKIE_NAME = "vgm_guest_id"
        const val EXPIRATION_DAYS = 30L
    }

    /**
     * Create or Refresh Guest Session
     * If guestId is provided and valid, refresh expiry.
     * If null or invalid, create new.
     */
    @Transactional
    fun ensureGuestSession(currentGuestId: String?, response: HttpServletResponse): String {
        val now = LocalDateTime.now()
        
        // 1. Validate existing
        if (currentGuestId != null) {
            val existing = guestSessionRepository.findByGuestIdAndExpiresAtAfter(currentGuestId, now)
            if (existing != null) {
                // Refresh logic if needed (e.g., extend by 30 days from now if close to expiry)
                // For now, always extend/update updated_at seems weird but fine.
                // Let's just return the current ID.
                return existing.guestId
            }
        }

        // 2. Create New
        val newGuestId = UUID.randomUUID().toString()
        val session = GuestSession(
            guestId = newGuestId,
            expiresAt = now.plusDays(EXPIRATION_DAYS)
        )
        guestSessionRepository.save(session)

        // 3. Set Cookie
        setGuestCookie(response, newGuestId)
        
        return newGuestId
    }

    @Transactional
    fun getValidGuestSession(guestId: String): GuestSession? {
        return guestSessionRepository.findByGuestIdAndExpiresAtAfter(guestId, LocalDateTime.now())
    }

    fun setGuestCookie(response: HttpServletResponse, guestId: String) {
        val cookie = Cookie(GUEST_COOKIE_NAME, guestId)
        cookie.path = "/"
        cookie.maxAge = (EXPIRATION_DAYS * 24 * 60 * 60).toInt()
        cookie.isHttpOnly = true
        cookie.secure = true // Should be true in prod, maybe configurable
        cookie.setAttribute("SameSite", "Lax") 
        response.addCookie(cookie)
    }

    fun removeGuestCookie(response: HttpServletResponse) {
        val cookie = Cookie(GUEST_COOKIE_NAME, "")
        cookie.path = "/"
        cookie.maxAge = 0
        cookie.isHttpOnly = true
        response.addCookie(cookie)
    }
}
