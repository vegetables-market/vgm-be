package com.example.myapp.service.market.favorite

import com.example.myapp.dto.market.*
import com.example.myapp.dto.market.item.ItemResponse
import com.example.myapp.dto.market.item.SellerInfo
import com.example.myapp.repository.market.category.CategoryRepository
import com.example.myapp.repository.market.item.ItemFavoriteRepository
import com.example.myapp.repository.market.item.ItemImageRepository
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.user.profile.UserProfileRepository
import org.springframework.stereotype.Service

/**
 * お気に入り一覧取得ユースケース
 */
@Service
class GetFavorites(
    private val itemFavoriteRepository: ItemFavoriteRepository,
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userProfileRepository: UserProfileRepository
) {

    operator fun invoke(userId: Int?, guestId: String?, page: Int, limit: Int): PaginatedResponse<ItemResponse> {
        val favorites = if (userId != null) {
            itemFavoriteRepository.findByUserId(userId)
        } else if (guestId != null) {
            itemFavoriteRepository.findByGuestId(guestId)
        } else {
            emptyList()
        }
        
        val itemIds = favorites.map { it.itemId }

        if (itemIds.isEmpty()) {
            return PaginatedResponse(
                items = emptyList(),
                pagination = PaginationInfo(
                    page = page,
                    limit = limit,
                    total = 0,
                    totalPages = 0
                )
            )
        }

        val items = itemRepository.findAllById(itemIds)

        val itemResponses = items.map { item ->
            val category = item.categoryId?.let { categoryRepository.findById(it).orElse(null) }
            val thumbnail = itemImageRepository.findByItemIdOrderByDisplayOrder(item.itemId!!).firstOrNull()
            // ゲストがいいねした場合でも出品者はユーザーなのでこれはOK
            val profile = userProfileRepository.findById(item.user.userId).orElse(null)

            ItemResponse(
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

        // ページネーション処理 (メモリ内)
        // 注意: 本来はDBでページネーションすべきだが、itemFavoriteRepositoryがListを返すためこのままにする
        val start = (page - 1) * limit
        val end = minOf(start + limit, itemResponses.size)
        val paginatedItems = if (start < itemResponses.size) {
            itemResponses.subList(start, end)
        } else {
            emptyList()
        }

        return PaginatedResponse(
            items = paginatedItems,
            pagination = PaginationInfo(
                page = page,
                limit = limit,
                total = itemResponses.size.toLong(),
                totalPages = (itemResponses.size + limit - 1) / limit
            )
        )
    }
}
