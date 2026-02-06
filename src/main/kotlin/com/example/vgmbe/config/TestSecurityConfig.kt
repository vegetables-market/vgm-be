package com.example.vgmbe.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * テスト用: すべてのリクエストを許可する簡易セキュリティ設定
 * - 開発中の手動テスト用にのみ追加。あとで必ず削除またはプロファイル限定してください。
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
class TestSecurityConfig {
    @Bean
    fun testFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
        return http.build()
    }
}
