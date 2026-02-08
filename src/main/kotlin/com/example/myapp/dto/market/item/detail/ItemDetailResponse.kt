package com.example.myapp.dto.market.item.detail

import com.example.myapp.dto.market.item.ItemResponse

/**
 * 詳細商品情報レスポンスDTO
 *
 * 商品詳細ページで使用される。
 *
 * Used in: [com.example.myapp.controller.market.item.search.ItemDetailController]
 */

data class ItemDetailResponse(
    val item: ItemDetail,
    val relatedItems: List<ItemResponse>
)

/**
 * 商品詳細情報DTO
 *
 * @property itemId 商品ID
 * @property title 商品名
 * @property description 説明
 * @property price 価格
 * @property quantity 在庫数
 * @property categoryId カテゴリID
 * @property categoryName カテゴリ名
 * @property condition 商品の状態
 * @property status ステータス
 * @property likesCount いいね数
 * @property isLiked 自分がいいねしているか
 * @property brand ブランド名
 * @property weight 重量
 * @property shippingPayerType 配送料負担
 * @property images 画像リスト
 * @property seller 出品者情報
 * @property createdAt 作成日時
 * @property updatedAt 更新日時
 */

data class ItemDetail(
    val itemId: Long,
    val title: String,
    val description: String?,
    val price: Int,
    val quantity: Int,
    val categoryId: Long?,
    val categoryName: String?,
    val condition: Int,
    val status: Short,
    val likesCount: Int,
    val isLiked: Boolean,
    val brand: String?,
    val weight: Int?,
    val shippingPayerType: Int,
    val images: List<ItemImageInfo>,
    val seller: SellerDetailInfo,
    val createdAt: String,
    val updatedAt: String
)

data class ItemImageInfo(
    val imageId: Long,
    val imageUrl: String,
    val displayOrder: Int
)

data class SellerDetailInfo(
    val userId: Int,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val ratingAverage: Double?,
    val ratingCount: Int
)
