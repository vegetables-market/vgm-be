package com.example.myapp.service.auth.session

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class AppCookieService {

    companion object {
        const val SESSION_COOKIE_NAME = "vgm_session"
        const val SESSION_MAX_AGE = 30 * 24 * 60 * 60 // 30 days
    }

    /**
     * セッションCookieをレスポンスに追加する
     */
    fun addSessionCookie(response: HttpServletResponse, sessionKey: String) {
        val cookie = Cookie(SESSION_COOKIE_NAME, sessionKey)
        cookie.isHttpOnly = true
        cookie.path = "/"
        cookie.maxAge = SESSION_MAX_AGE
        // cookie.secure = true // Production only (should be configured via properties)

        response.addCookie(cookie)
    }

    /**
     * セッションCookieを削除する（ログアウト時など）
     */
    fun removeSessionCookie(response: HttpServletResponse) {
        val cookie = Cookie(SESSION_COOKIE_NAME, "")
        cookie.isHttpOnly = true
        cookie.path = "/"
        cookie.maxAge = 0 // Delete immediately

        response.addCookie(cookie)
    }

    /**
     * リクエストからセッションCookieを取得する
     */
    fun getSessionCookie(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == SESSION_COOKIE_NAME }?.value
    }

    /**
     * リクエストからゲストCookieを取得する
     */
    fun getGuestCookie(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == "vgm_guest_id" }?.value // GuestSessionService.GUEST_COOKIE_NAME
    }
}