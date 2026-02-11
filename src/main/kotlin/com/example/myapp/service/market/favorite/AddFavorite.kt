package com.example.myapp.service.market.favorite

import com.example.myapp.entity.market.item.ItemFavorite
import com.example.myapp.repository.market.item.ItemFavoriteRepository
import com.example.myapp.repository.market.item.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * お気に入り追加ユースケース
 */
@Service
class AddFavorite(
    private val itemFavoriteRepository: ItemFavoriteRepository,
    private val itemRepository: ItemRepository
) {

    @Transactional
    operator fun invoke(userId: Int?, guestId: String?, displayId: String) {
        if (userId == null && guestId == null) {
            throw IllegalArgumentException("User ID or Guest ID required")
        }

        val item = itemRepository.findByDisplayId(displayId)
            ?: throw IllegalArgumentException("商品が見つかりません")
        val itemId = item.itemId!!

        // 既に存在する場合は何もしない
        val exists = if (userId != null) {
            itemFavoriteRepository.existsByUserIdAndItemId(userId, itemId)
        } else {
            itemFavoriteRepository.existsByGuestIdAndItemId(guestId!!, itemId)
        }
        
        if (exists) return

        val favorite = ItemFavorite(
            userId = userId,
            guestId = guestId,
            itemId = itemId
        )
        itemFavoriteRepository.save(favorite)
    }
}
