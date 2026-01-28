package com.example.myapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Spring Security 設定
 * 
 * 主な機能:
 * - CSRF保護（CookieベースのCSRFトークン）
 * - CORS設定
 * - セッション管理
 * - Cookie設定（HttpOnly, SameSite）
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    @Value("\${cors.allowed-origins}") private val allowedOrigins: String,
    private val sessionAuthenticationFilter: com.example.myapp.security.SessionAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF保護を有効化（ただし認証前エンドポイントは除外）
            .csrf { csrf ->
                csrf
                    // CookieベースのCSRFトークン（JavaScriptから読み取り可能）
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    // 認証前のエンドポイントではCSRF保護を無効化
                    .ignoringRequestMatchers(
                        "/v1/auth/signup",
                        "/v1/auth/login",
                        "/v1/auth/verify-email",
                        "/v1/auth/verify-challenge",
                        "/v1/auth/verify-challenge", // チャレンジ認証
                        "/v1/auth/resend-code",      // コード再送
                        "/v1/auth/verify-mfa",       // MFA認証
                        "/v1/user/mfa/**",  // MFAエンドポイント(独自認証)
                        "/v1/auth/logout",   // ログアウト(独自認証)
                        "/v1/market/items/upload-token", // 一時的なCSRF除外(デバッグ)
                        "/v1/market/items/**",     // 商品関連API(一時的除外)
                        "/v1/admin/media/upload-token"      // 管理者アップロードも除外
                    )
            }
            
            // CORS設定
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }
            
            // 認証・認可設定
            .authorizeHttpRequests { authorize ->
                authorize
                    // 公開エンドポイント（認証不要）
                    .requestMatchers(
                        "/v1/auth/signup",           // 新規登録
                        "/v1/auth/login",            // ログイン
                        "/v1/auth/verify-email",     // メール認証
                        "/v1/auth/verify-challenge", // チャレンジ認証
                        "/v1/auth/resend-code",      // コード再送
                        "/v1/auth/verify-mfa",       // MFA認証
                        "/health",
                        "/actuator/health",
                        "/error"
                    ).permitAll()

                    // 独自セッション認証を使用するエンドポイント（Controller内で認証チェック）
                    .requestMatchers(
                        "/v1/user/mfa/**",           // MFA設定
                        "/v1/auth/logout",           // ログアウト
                        "/v1/market/items/**"        // デバッグ用: 一時的に許可
                    ).permitAll()
                    
                    // その他のエンドポイントは認証必須
                    // ※個別のコントローラーで @PreAuthorize を使用することも可能
                    .anyRequest().authenticated()
            }
            
            // セッション管理
            .sessionManagement { session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1) // 1ユーザーあたり最大1セッション
                    .maxSessionsPreventsLogin(false) // 新しいログインで古いセッションを無効化
            }
            
            // ログアウト設定 (無効化し、LoginControllerで独自実装を使用)
            .logout { it.disable() }

            // セッション認証フィルターを追加
            .addFilterBefore(sessionAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * CORS設定
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val origins = allowedOrigins.split(",").map { it.trim() }
        
        val configuration = CorsConfiguration().apply {
            allowedOrigins = origins
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("*")
            allowCredentials = true
            exposedHeaders = listOf("Set-Cookie", "X-XSRF-TOKEN")
            maxAge = 3600L // プリフライトリクエストのキャッシュ時間（秒）
        }
        
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    /**
     * パスワードエンコーダー
     */
    @Bean
    fun passwordEncoder(): org.springframework.security.crypto.password.PasswordEncoder {
        return org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
    }
}
