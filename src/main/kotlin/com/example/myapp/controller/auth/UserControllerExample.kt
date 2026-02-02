package com.example.myapp.controller.auth

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

/**
 * ユーザー情報コントローラー（認証必須の実装例）
 * 
 * 認証チェックの実装方法:
 * 1. @PreAuthorize アノテーション
 * 2. @AuthenticationPrincipal で現在のユーザーを取得
 * 3. 手動でSecurityContextから取得
 */
@RestController
@RequestMapping("/v1/user")
class UserController {

    /**
     * 方法1: @PreAuthorize アノテーションを使用
     * 
     * - クラスまたはメソッドレベルで指定可能
     * - 認証されていない場合は自動的に401を返す
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    fun getUserProfile(@AuthenticationPrincipal userDetails: UserDetails?): Map<String, Any> {
        // userDetails が null の場合は401が自動的に返される
        return mapOf(
            "user_id" to (userDetails?.username ?: "unknown"),
            "message" to "User profile data"
        )
    }

    /**
     * 方法2: @AuthenticationPrincipal を使用してマニュアルチェック
     * 
     * - より柔軟な制御が可能
     * - カスタムエラーレスポンスを返せる
     */
    @GetMapping("/settings")
    fun getUserSettings(@AuthenticationPrincipal userDetails: UserDetails?): Map<String, Any> {
        // マニュアルで認証チェック
        if (userDetails == null) {
            throw UnauthorizedException("Authentication required")
        }

        return mapOf(
            "user_id" to userDetails.username,
            "settings" to mapOf(
                "theme" to "dark",
                "language" to "ja"
            )
        )
    }

    /**
     * 方法3: SecurityContext から手動で取得
     * 
     * - 最も柔軟な方法
     * - 必要に応じて使用
     */
    @GetMapping("/dashboard")
    fun getUserDashboard(): Map<String, Any> {
        val authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .authentication

        if (authentication == null || !authentication.isAuthenticated) {
            throw UnauthorizedException("Authentication required")
        }

        val username = authentication.name

        return mapOf(
            "user_id" to username,
            "dashboard_data" to "Some data"
        )
    }
}

/**
 * カスタム例外クラス
 */
class UnauthorizedException(message: String) : RuntimeException(message)

// Note: GlobalExceptionHandler は config パッケージに実装済み

