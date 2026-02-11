package com.example.myapp.dto.market.item.liisting

/**
 * 下書き作成レスポンスDTO
 *
 * @property itemId 作成された下書き商品のID
 * Used in: [com.example.myapp.controller.market.item.listing.CreateDraftController]
 */

data class CreateDraftResponse(
    val itemId: String
)
