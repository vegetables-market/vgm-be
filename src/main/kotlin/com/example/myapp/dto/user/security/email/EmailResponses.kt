package com.example.myapp.dto.user.security.email

import java.time.LocalDateTime

/**
 * メールアドレス情報レスポンスDTO
 *
 * @property emailId メールアドレスID
 * @property email メールアドレス
 * @property isPrimary メインメールアドレスかどうか
 * @property isVerified 確認済みかどうか
 * @property createdAt 作成日時
 * Used in: [com.example.myapp.controller.user.security.email.GetEmailsController]
 */

data class EmailResponse(
    val emailId: Long,
    val email: String,
    val isPrimary: Boolean,
    val isVerified: Boolean,
    val createdAt: LocalDateTime
)
