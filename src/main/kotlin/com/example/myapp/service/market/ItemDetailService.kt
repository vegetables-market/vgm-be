package com.example.myapp.service.market

import com.example.myapp.dto.market.*
import com.example.myapp.repository.market.CategoryRepository
import com.example.myapp.repository.market.ItemFavoriteRepository
import com.example.myapp.repository.market.ItemImageRepository
import com.example.myapp.repository.market.ItemRepository
import com.example.myapp.repository.user.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

/**
 * 商品詳細サービス
 */
@Service
class ItemDetailService(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userProfileRepository: UserProfileRepository,
    private val itemFavoriteRepository: ItemFavoriteRepository
) {

    /**
     * 商品詳細取得
     */
    fun getItemDetail(itemId: Long, userId: Int?, guestId: String?): ItemDetailResponse? {
        val item = itemRepository.findById(itemId).orElse(null) ?: return null
        
        // 画像取得
        val images = itemImageRepository.findByItemIdOrderByDisplayOrder(itemId).map {
            ItemImageInfo(
                imageId = it.imageId!!,
                imageUrl = it.imageUrl,
                displayOrder = it.displayOrder
            )
        }

        // カテゴリー取得
        val category = item.categoryId?.let { categoryRepository.findById(it).orElse(null) }

        // 出品者プロフィール取得
        val profile = userProfileRepository.findById(item.user.userId).orElse(null)
        
        // お気に入り状態確認
        val isLiked = if (userId != null) {
            itemFavoriteRepository.existsByUserIdAndItemId(userId, itemId)
        } else if (guestId != null) {
            itemFavoriteRepository.existsByGuestIdAndItemId(guestId, itemId)
        } else {
            false
        }

        // 評価平均計算
        val ratingAverage = if (profile != null && profile.ratingCount > 0) {
            profile.ratingSum.toDouble() / profile.ratingCount
        } else null

        val itemDetail = ItemDetail(
            itemId = item.itemId!!,
            title = item.name ?: "",
            description = item.description,
            price = item.price ?: 0,
            quantity = item.quantity,
            categoryId = item.categoryId,
            categoryName = category?.name,
            condition = item.itemCondition,
            status = item.status,
            likesCount = item.likesCount,
            isLiked = isLiked,
            brand = item.brand,
            weight = item.weight,
            shippingPayerType = item.shippingPayerType,
            images = images,
            seller = SellerDetailInfo(
                userId = item.user.userId,
                username = item.user.username,
                displayName = item.user.displayName,
                avatarUrl = profile?.profileImageUrl,
                ratingAverage = ratingAverage,
                ratingCount = profile?.ratingCount ?: 0
            ),
            createdAt = item.createdAt.toString(),
            updatedAt = item.updatedAt.toString()
        )

        // 関連商品取得（同じカテゴリー優先、次に同じ出品者）
        val relatedItems = getRelatedItems(item.itemId!!, item.categoryId, item.user.userId)

        return ItemDetailResponse(
            item = itemDetail,
            relatedItems = relatedItems
        )
    }

    private fun getRelatedItems(itemId: Long, categoryId: Long?, sellerId: Int): List<ItemResponse> {
        val pageable = PageRequest.of(0, 6)
        val status: Short = 2 // 2:出品中

        val items = if (categoryId != null) {
            // 同じカテゴリーの商品
            itemRepository.findByCategoryIdAndStatusAndItemIdNotOrderByCreatedAtDesc(
                categoryId, status, itemId, pageable
            ).content
        } else {
            // 同じ出品者の他の商品
            itemRepository.findByUser_UserIdAndStatusAndItemIdNotOrderByCreatedAtDesc(
                sellerId, status, itemId, pageable
            ).content
        }

        return items.map { item ->
            val category = item.categoryId?.let { categoryRepository.findById(it).orElse(null) }
            val thumbnail = itemImageRepository.findByItemIdOrderByDisplayOrder(item.itemId!!).firstOrNull()
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
    }
}
