package com.example.myapp.dto.market.item

/**
 * 商品情報レスポンスDTO (汎用/関連商品用)
 *
 * `ItemDetailResponse` の関連商品リストなどで使用される。
 * `SimpleItemResponse` と似ているが、こちらは出品者情報 (`SellerInfo`) を含む詳細版。
 *
 * @property itemId 商品ID
 * @property title 商品名
 * @property description 説明
 * @property price 価格
 * @property categoryId カテゴリID
 * @property categoryName カテゴリ名
 * @property condition 商品の状態
 * @property status ステータス
 * @property likesCount いいね数
 * @property thumbnailUrl サムネイル画像URL
 * @property seller 出品者情報
 * @property createdAt 作成日時
 * Used in: [com.example.myapp.dto.market.item.detail.ItemDetailResponse]
 */

data class ItemResponse(
    val itemId: Long,
    val title: String,
    val description: String?,
    val price: Int,
    val categoryId: Long?,
    val categoryName: String?,
    val condition: Int,
    val status: Short,
    val likesCount: Int,
    val thumbnailUrl: String?,
    val seller: SellerInfo,
    val createdAt: String
)
