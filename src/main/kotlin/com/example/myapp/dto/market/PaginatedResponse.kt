package com.example.myapp.dto.market

/**
 * ページネーションレスポンスDTO (汎用)
 *
 * リスト形式のデータをページネーション情報と共に返却する際に使用するラッパー。
 *
 * @param T データ型
 * @property items データリスト
 * @property pagination ページネーション情報
 * Used in: [com.example.myapp.controller.market.item.search.ItemSearchController], [com.example.myapp.controller.market.favorite.GetFavoritesController]
 */

data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: PaginationInfo
)

/**
 * ページネーション情報DTO
 *
 * @property page 現在のページ番号
 * @property limit 1ページあたりの件数
 * @property total 全要素数
 * @property totalPages 全ページ数
 */



data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)
