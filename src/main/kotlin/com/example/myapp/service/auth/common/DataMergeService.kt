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
        // 1. Merge Cart Items
        val guestCartItems = cartItemRepository.findByGuestId(guestId)
        val userCartItems = cartItemRepository.findByUserId(userId)

        guestCartItems.forEach { guestItem ->
            val existingUserItem = userCartItems.find { it.itemId == guestItem.itemId }

            if (existingUserItem != null) {
                // Determine new quantity (Sum)
                existingUserItem.quantity += guestItem.quantity
                cartItemRepository.save(existingUserItem)
                // Delete guest item
                cartItemRepository.delete(guestItem)
            } else {
                // Move assignment to User
                // Since fields are 'val', we verify if we can update or need to recreate.
                // Assuming 'val', we recreate.
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

        // 2. Merge Likes (Favorites)
        val guestLikes = itemFavoriteRepository.findByGuestId(guestId)
        val userLikes = itemFavoriteRepository.findByUserId(userId) // Optimization: Get IDs

        val userLikedItemIds = userLikes.map { it.itemId }.toSet()

        guestLikes.forEach { guestLike ->
            if (userLikedItemIds.contains(guestLike.itemId)) {
                // Duplicate: User already likes it.
                // Just delete guest like.
                itemFavoriteRepository.delete(guestLike)
            } else {
                // Move to User
                val newLike = ItemFavorite(
                    userId = userId,
                    guestId = null,
                    itemId = guestLike.itemId
                )
                itemFavoriteRepository.save(newLike)
                itemFavoriteRepository.delete(guestLike)
            }
        }

        // 3. Cleanup Guest Session
        // This will cascade delete triggers if checking constraints, 
        // but we already deleted or moved items so it should be clean.
        guestSessionRepository.deleteById(guestId)
    }
}
