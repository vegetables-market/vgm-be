package com.example.myapp.dto.user.account

/**
 * ユーザープロフィール詳細情報DTO
 *
 * マイページ等で表示する自身の詳細情報。
 *
 * @property userId ユーザーID
 * @property username ユーザー名
 * @property displayName 表示名
 * @property email メールアドレス
 * @property avatarUrl アバター画像URL
 * @property hasPassword パスワード設定済みかどうか
 * @property role ロール
 * @property isEmailVerified メールアドレス確認済み状態
 * Used in: [com.example.myapp.controller.user.account.MyAccountController]
 */

data class UserProfileInfo(
    val userId: Int,
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String?,
    val bio: String?,
    val hasPassword: Boolean,
    val role: String,
    val isEmailVerified: Boolean
)
