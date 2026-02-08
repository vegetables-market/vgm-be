package com.example.myapp.dto.user.profile

/**
 * パスワード変更リクエストDTO
 *
 * @property currentPassword 現在のパスワード
 * @property newPassword 新しいパスワード
 * Used in: [com.example.myapp.controller.user.security.password.PasswordUpdateController]
 */
data class ChangePasswordRequest(
    val currentPassword: String?,
    val newPassword: String
)

/**
 * ユーザー名変更リクエストDTO
 *
 * @property newUsername 新しいユーザー名
 * @property password パスワード (本人確認用)
 * Used in: [com.example.myapp.controller.user.account.UpdateUsernameController]
 */
data class ChangeUsernameRequest(
    val newUsername: String,
    val password: String?
)
