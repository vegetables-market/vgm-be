package com.example.myapp.dto.market.item.liisting

/**
 * 画像紐付けリクエストDTO
 *
 * アップロードされた画像を商品に紐付ける。
 *
 * @property filenames 画像ファイル名リスト
 * Used in: [com.example.myapp.controller.market.item.listing.LinkImagesController]
 */

data class LinkImagesRequest(
    val filenames: List<String>,
    val replaceExisting: Boolean = false
)
