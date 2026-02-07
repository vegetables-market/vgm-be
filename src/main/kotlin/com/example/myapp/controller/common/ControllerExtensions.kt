package com.example.myapp.controller.common

import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime

/**
 * 共通コントローラー拡張関数
 */

fun HttpServletRequest.getAppUser(
    appCookieService: AppCookieService,
    sessionService: SessionService
): Pair<Int?, String?> {
    val userId = getUserIdFromSession(this, appCookieService, sessionService)
    val guestId = getGuestIdFromCookie(this, appCookieService)
    return Pair(userId, guestId)
}

private fun getUserIdFromSession(
    request: HttpServletRequest,
    appCookieService: AppCookieService,
    sessionService: SessionService
): Int? {
    val sessionKey = appCookieService.getSessionCookie(request) ?: return null
    // getValidSession returns null if session is invalid, expired, or revoked
    val session = sessionService.getValidSession(sessionKey) ?: return null
    return session.userId
}

private fun getGuestIdFromCookie(
    request: HttpServletRequest,
    appCookieService: AppCookieService
): String? {
    return appCookieService.getGuestCookie(request)
}
