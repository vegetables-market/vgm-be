package com.example.myapp.dto.market.item

/**
 * 簡易商品情報レスポンスDTO
 *
 * 出品リストや検索結果などで使用される軽量な商品情報。
 *
 * @property id 商品ID
 * @property name 商品名
 * @property price 価格
 * @property status ステータス
 * @property imageUrl サムネイル画像URL
 * @property createdAt 作成日時
 * Used in: [com.example.myapp.controller.market.item.listing.MyItemsController], [com.example.myapp.controller.market.item.listing.PublishItemController]
 */

data class SimpleItemResponse(
    val id: Long,
    val name: String?,
    val price: Int?,
    val status: Int,
    val imageUrl: String?, // 1枚目
    val createdAt: String
)
