package com.example.myapp.dto.user.session

import java.time.LocalDateTime

/**
 * セッション情報レスポンスDTO
 *
 * @property sessionId セッションID
 * @property deviceInfo デバイス情報 (User-Agent string etc)
 * @property ipAddress IPアドレス
 * @property createdAt 作成日時
 * @property lastActiveAt 最終アクティブ日時
 * @property expiresAt 有効期限
 * @property isCurrent 現在のセッションかどうか
 * Used in: [com.example.myapp.controller.user.session.SessionReadController]
 */

data class SessionResponse(
    val sessionId: Long,
    val deviceInfo: String?,
    val ipAddress: String?,
    val createdAt: LocalDateTime,
    val lastActiveAt: LocalDateTime?,
    val expiresAt: LocalDateTime,
    val isCurrent: Boolean,
    val provider: String?
)
