package com.example.myapp.security

import com.example.myapp.repository.auth.GuestSessionRepository
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.repository.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime

/**
 * セッション認証フィルター
 *
 * リクエストのCookie（`vgm_session` または `vgm_guest_id`）を確認し、
 * DB上の有効なセッションと照合して、Spring Securityのコンテキストに認証情報をセットします。
 *
 * - `vgm_session`: ログイン済みユーザーのセッション
 * - `vgm_guest_id`: 未ログインゲストのセッション
 */
@Component
class SessionAuthenticationFilter(
    private val userSessionRepository: UserSessionRepository,
    private val userRepository: UserRepository,
    private val guestSessionRepository: GuestSessionRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 既に認証済みの場合はスキップ
        if (SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        // 1. 会員ログインチェック (vgm_session)
        val sessionCookie = request.cookies?.find { it.name == "vgm_session" }
        val sessionKey = sessionCookie?.value

        if (!sessionKey.isNullOrBlank()) {
            try {
                // DBから有効なセッションを検索
                val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
                    sessionKey, 
                    LocalDateTime.now()
                )

                if (session != null) {
                    // ユーザー情報を取得してロールを確認
                    val user = userRepository.findById(session.userId).orElse(null)

                    if (user != null) {
                        // 権限リスト作成
                        val roleName = "ROLE_${user.role}" 
                        val authorities = listOf(SimpleGrantedAuthority(roleName))

                        val auth = UsernamePasswordAuthenticationToken(
                            session, 
                            null,    
                            authorities
                        )

                        SecurityContextHolder.getContext().authentication = auth
                        filterChain.doFilter(request, response)
                        return
                    }
                }
            } catch (e: Exception) {
                // セッション検証中のエラーは無視
            }
        }

        // 2. ゲストセッションチェック (vgm_guest_id)
        val guestCookie = request.cookies?.find { it.name == "vgm_guest_id" }
        val guestId = guestCookie?.value

        if (!guestId.isNullOrBlank()) {
            try {
                val guestSession = guestSessionRepository.findByGuestIdAndExpiresAtAfter(
                    guestId,
                    LocalDateTime.now()
                )

                if (guestSession != null) {
                     // ゲストとして認証
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_GUEST"))
                    val auth = UsernamePasswordAuthenticationToken(
                        guestSession,
                        null,
                        authorities
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            } catch (e: Exception) {
                // 無視
            }
        }

        filterChain.doFilter(request, response)
    }
}
