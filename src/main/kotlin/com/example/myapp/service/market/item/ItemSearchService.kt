package com.example.myapp.service.market.item

import com.example.myapp.dto.market.*
import com.example.myapp.dto.market.item.ItemResponse
import com.example.myapp.dto.market.item.search.ItemSearchRequest
import com.example.myapp.dto.market.item.SellerInfo
import com.example.myapp.entity.market.item.Item
import com.example.myapp.repository.market.category.CategoryRepository
import com.example.myapp.repository.market.item.ItemFavoriteRepository
import com.example.myapp.repository.market.item.ItemImageRepository
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.user.profile.UserProfileRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * 商品検索サービス
 */
@Service
class ItemSearchService(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userProfileRepository: UserProfileRepository,
    private val itemFavoriteRepository: ItemFavoriteRepository
) {

    /**
     * 商品検索
     */
    fun searchItems(request: ItemSearchRequest, userId: Int?): PaginatedResponse<ItemResponse> {
        val pageable: Pageable = PageRequest.of(request.page - 1, request.limit.coerceAtMost(100))
        val status: Short = 2 // 2:出品中

        val itemsPage: Page<Item> = when {
            // キーワード + カテゴリー
            !request.q.isNullOrBlank() && request.categoryId != null -> {
                itemRepository.searchByKeywordAndCategory(request.q, request.categoryId, status, pageable)
            }
            // キーワード + 価格範囲
            !request.q.isNullOrBlank() && request.minPrice != null && request.maxPrice != null -> {
                itemRepository.searchByKeywordAndPriceRange(request.q, request.minPrice, request.maxPrice, status, pageable)
            }
            // キーワードのみ
            !request.q.isNullOrBlank() -> {
                itemRepository.searchByKeyword(request.q, status, pageable)
            }
            // カテゴリーのみ
            request.categoryId != null -> {
                itemRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(request.categoryId, status, pageable)
            }
            // 価格範囲のみ
            request.minPrice != null && request.maxPrice != null -> {
                itemRepository.findByStatusAndPriceBetweenOrderByCreatedAtDesc(status, request.minPrice, request.maxPrice, pageable)
            }
            // ソートのみ
            else -> {
                when (request.sort) {
                    "popular" -> itemRepository.findByStatusOrderByLikesCountDescCreatedAtDesc(status, pageable)
                    "price_asc" -> itemRepository.findByStatusOrderByPriceAscCreatedAtDesc(status, pageable)
                    "price_desc" -> itemRepository.findByStatusOrderByPriceDescCreatedAtDesc(status, pageable)
                    else -> itemRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                }
            }
        }

        val items = itemsPage.content.map { toItemResponse(it) }

        return PaginatedResponse(
            items = items,
            pagination = PaginationInfo(
                page = request.page,
                limit = request.limit,
                total = itemsPage.totalElements,
                totalPages = itemsPage.totalPages
            )
        )
    }

    private fun toItemResponse(item: Item): ItemResponse {
        val category = item.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val thumbnail = itemImageRepository.findByItemIdOrderByDisplayOrder(item.itemId!!).firstOrNull()
        val profile = userProfileRepository.findById(item.user.userId).orElse(null)

        return ItemResponse(
            itemId = item.itemId,
            title = item.name ?: "",
            description = item.description,
            price = item.price ?: 0,
            categoryId = item.categoryId,
            categoryName = category?.name,
            condition = item.itemCondition,
            status = item.status,
            likesCount = item.likesCount,
            thumbnailUrl = thumbnail?.imageUrl,
            seller = SellerInfo(
                userId = item.user.userId,
                username = item.user.username,
                displayName = item.user.displayName,
                avatarUrl = profile?.profileImageUrl
            ),
            createdAt = item.createdAt.toString()
        )
    }
}
