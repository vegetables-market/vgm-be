package com.example.myapp.dto.market.item

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