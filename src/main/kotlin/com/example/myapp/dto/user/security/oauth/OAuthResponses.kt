package com.example.myapp.dto.user.security.oauth

import java.time.LocalDateTime

/**
 * OAuth連携情報レスポンスDTO
 *
 * @property connectionId 連携ID
 * @property provider プロバイダ名 (google, github, etc)
 * @property providerEmail プロバイダ側のメールアドレスまたは表示名
 * @property connectedAt 連携日時
 * Used in: [com.example.myapp.controller.user.security.oauth.GetOAuthConnectionsController]
 */

data class OAuthConnectionResponse(
    val connectionId: Long,
    val provider: String,
    val providerEmail: String?,
    val connectedAt: LocalDateTime
)
