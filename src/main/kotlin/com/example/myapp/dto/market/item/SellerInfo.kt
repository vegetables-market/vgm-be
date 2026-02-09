package com.example.myapp.dto.market.item

/**
 * 出品者情報DTO
 *
 * 商品情報に含まれる出品者の簡易情報。
 *
 * @property userId ユーザーID
 * @property username ユーザー名
 * @property displayName 表示名
 * @property avatarUrl アバター画像URL
 * Used in: [com.example.myapp.dto.market.item.ItemResponse], [com.example.myapp.dto.market.item.detail.ItemDetailResponse]
 */

data class SellerInfo(
    val userId: Int,
    val username: String,
    val displayName: String,
    val avatarUrl: String?
)
