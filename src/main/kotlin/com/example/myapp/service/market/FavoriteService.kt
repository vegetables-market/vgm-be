package com.example.myapp.service.market

import com.example.myapp.dto.market.*
import com.example.myapp.dto.market.item.ItemResponse
import com.example.myapp.dto.market.item.SellerInfo
import com.example.myapp.entity.market.item.ItemFavorite
import com.example.myapp.repository.market.CategoryRepository
import com.example.myapp.repository.market.ItemFavoriteRepository
import com.example.myapp.repository.market.ItemImageRepository
import com.example.myapp.repository.market.ItemRepository
import com.example.myapp.repository.user.UserProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * お気に入り管理サービス
 */
@Service
class FavoriteService(
    private val itemFavoriteRepository: ItemFavoriteRepository,
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * お気に入りに追加
     */
    @Transactional
    fun addFavorite(userId: Int?, guestId: String?, itemId: Long) {
        if (userId == null && guestId == null) {
            throw IllegalArgumentException("User ID or Guest ID required")
        }

        // 既に存在する場合は何もしない
        val exists = if (userId != null) {
            itemFavoriteRepository.existsByUserIdAndItemId(userId, itemId)
        } else {
            itemFavoriteRepository.existsByGuestIdAndItemId(guestId!!, itemId)
        }
        
        if (exists) return

        // 商品が存在するか確認
        if (!itemRepository.existsById(itemId)) {
            throw IllegalArgumentException("商品が見つかりません")
        }

        val favorite = ItemFavorite(
            userId = userId,
            guestId = guestId,
            itemId = itemId
        )
        itemFavoriteRepository.save(favorite)
    }

    /**
     * お気に入りから削除
     */
    @Transactional
    fun removeFavorite(userId: Int?, guestId: String?, itemId: Long) {
        if (userId != null) {
            itemFavoriteRepository.deleteByUserIdAndItemId(userId, itemId)
        } else if (guestId != null) {
            itemFavoriteRepository.deleteByGuestIdAndItemId(guestId, itemId)
        }
    }

    /**
     * お気に入り一覧取得
     */
    fun getFavorites(userId: Int?, guestId: String?, page: Int, limit: Int): PaginatedResponse<ItemResponse> {
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
