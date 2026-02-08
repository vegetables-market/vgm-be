package com.example.myapp.dto.market

/**
 * カテゴリ情報レスポンスDTO
 *
 * @property categoryId カテゴリID
 * @property categoryName カテゴリ名
 * @property parentId 親カテゴリID (トップレベルの場合はnull)
 * @property level 階層レベル
 * @property iconUrl アイコン画像URL
 * @property sortOrder 表示順
 * @property children 子カテゴリリスト
 * Used in: [com.example.myapp.controller.market.category.CategoryController]
 */

data class CategoryResponse(
    val categoryId: Long,
    val categoryName: String,
    val parentId: Long?,
    val level: Int,
    val iconUrl: String?,
    val sortOrder: Int,
    val children: List<CategoryResponse> = emptyList()
)
