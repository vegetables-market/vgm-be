package com.example.myapp.controller.market

import com.example.myapp.service.auth.AppCookieService
import com.example.myapp.service.auth.SessionService
import jakarta.servlet.http.HttpServletRequest

/**
 * マーケット系コントローラーの共通拡張関数
 */
fun HttpServletRequest.getMarketUser(
    appCookieService: AppCookieService, 
    sessionService: SessionService
): Pair<Int?, String?> {
    val sessionKey = appCookieService.getSessionCookie(this)
    val userId = sessionKey?.let { sessionService.getValidSession(it)?.userId }
    val guestId = if (userId == null) appCookieService.getGuestCookie(this) else null
    return userId to guestId
}
