package com.example.myapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/auth/**",
                    "/v1/auth/**",
                    "/api/items/**",
                    "/api/users/*/theme",
                    "/api/users/me/theme"
                ).permitAll() // 開発用: test.http に記載のエンドポイントを一時的に全解放（本番では必ず見直すこと）
                it.anyRequest().authenticated()
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }



            // .csrf { it.disable() } // REST API では CSRF を切る
            // .authorizeHttpRequests {
            //     it.requestMatchers(
            //         "/api/auth/register",
            //         "/api/auth/login",
            //         "/api/auth/totp/**"
            //     ).permitAll() // 認証不要
            //     it.anyRequest().authenticated() // それ以外は認証必要
            // }
            // .httpBasic { it.disable() } // Basic 認証を無効化
            // .formLogin { it.disable() } // フォームログインも無効化

        return http.build()
    }
}