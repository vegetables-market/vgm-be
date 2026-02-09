package com.example.myapp.service.market.favorite

import com.example.myapp.repository.market.item.ItemFavoriteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * お気に入り削除ユースケース
 */
@Service
class RemoveFavorite(
    private val itemFavoriteRepository: ItemFavoriteRepository
) {

    @Transactional
    operator fun invoke(userId: Int?, guestId: String?, itemId: Long) {
        if (userId != null) {
            itemFavoriteRepository.deleteByUserIdAndItemId(userId, itemId)
        } else if (guestId != null) {
            itemFavoriteRepository.deleteByGuestIdAndItemId(guestId, itemId)
        }
    }
}
