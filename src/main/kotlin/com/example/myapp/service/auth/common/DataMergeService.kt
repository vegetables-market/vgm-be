package com.example.myapp.service.auth.common

import com.example.myapp.entity.market.cart.CartItem
import com.example.myapp.entity.market.item.ItemFavorite
import com.example.myapp.repository.auth.GuestSessionRepository
import com.example.myapp.repository.market.cart.CartItemRepository
import com.example.myapp.repository.market.item.ItemFavoriteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataMergeService(
    private val cartItemRepository: CartItemRepository,
    private val itemFavoriteRepository: ItemFavoriteRepository,
    private val guestSessionRepository: GuestSessionRepository
) {

    @Transactional
    fun mergeGuestData(userId: Int, guestId: String) {
        // 1. カートアイテムの統合
        val guestCartItems = cartItemRepository.findByGuestId(guestId)
        val userCartItems = cartItemRepository.findByUserId(userId)

        guestCartItems.forEach { guestItem ->
            val existingUserItem = userCartItems.find { it.itemId == guestItem.itemId }

            if (existingUserItem != null) {
                // 数量を合算
                existingUserItem.quantity += guestItem.quantity
                cartItemRepository.save(existingUserItem)
                // ゲストアイテムを削除
                cartItemRepository.delete(guestItem)
            } else {
                // ユーザーに割り当て（再作成）
                val newItem = CartItem(
                    userId = userId,
                    guestId = null,
                    itemId = guestItem.itemId,
                    quantity = guestItem.quantity
                )
                cartItemRepository.save(newItem)
                cartItemRepository.delete(guestItem)
            }
        }

        // 2. お気に入り（いいね）の統合
        val guestLikes = itemFavoriteRepository.findByGuestId(guestId)
        val userLikes = itemFavoriteRepository.findByUserId(userId) // 最適化: IDを取得

        val userLikedItemIds = userLikes.map { it.itemId }.toSet()

        guestLikes.forEach { guestLike ->
            if (userLikedItemIds.contains(guestLike.itemId)) {
                // 重複: ユーザーは既にお気に入り済み。ゲストのお気に入りを削除
                itemFavoriteRepository.delete(guestLike)
            } else {
                // ユーザーへ移動
                val newLike = ItemFavorite(
                    userId = userId,
                    guestId = null,
                    itemId = guestLike.itemId
                )
                itemFavoriteRepository.save(newLike)
                itemFavoriteRepository.delete(guestLike)
            }
        }

        // 3. ゲストセッションのクリーンアップ
        guestSessionRepository.deleteById(guestId)
    }
}
