package com.example.myapp.service.market.favorite

import com.example.myapp.repository.market.item.ItemFavoriteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * お気に入り削除ユースケース
 */
@Service
class RemoveFavorite(
    private val itemFavoriteRepository: ItemFavoriteRepository,
    private val itemRepository: com.example.myapp.repository.market.item.ItemRepository
) {

    @Transactional
    operator fun invoke(userId: Int?, guestId: String?, displayId: String) {
        val item = itemRepository.findByDisplayId(displayId) ?: return // 商品がない場合は何もしない（削除済み扱い）
        val itemId = item.itemId!!

        if (userId != null) {
            itemFavoriteRepository.deleteByUserIdAndItemId(userId, itemId)
        } else if (guestId != null) {
            itemFavoriteRepository.deleteByGuestIdAndItemId(guestId, itemId)
        }
    }
}
