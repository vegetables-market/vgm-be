package com.example.myapp.dto.market.item.search

/**
 * 商品検索リクエストDTO
 *
 * @property q 検索キーワード
 * @property categoryId カテゴリID
 * @property minPrice 最低価格
 * @property maxPrice 最高価格
 * @property condition 商品コンディション
 * @property sort ソート順 (newest, price_asc, price_desc, popular)
 * @property page ページ番号
 * @property limit 1ページあたりの件数
 * Used in: [com.example.myapp.controller.market.item.search.ItemSearchController]
 */

data class ItemSearchRequest(
    val q: String? = null,  // 検索キーワード
    val categoryId: Long? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val condition: Int? = null,
    val sort: String = "newest",  // newest, price_asc, price_desc, popular
    val page: Int = 1,
    val limit: Int = 20
)