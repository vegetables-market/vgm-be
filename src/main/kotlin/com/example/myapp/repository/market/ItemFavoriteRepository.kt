package com.example.myapp.repository.market

import com.example.myapp.entity.market.item.ItemFavorite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemFavoriteRepository : JpaRepository<ItemFavorite, Long> {
    fun findByUserIdAndItemId(userId: Int, itemId: Long): ItemFavorite?
    fun findByGuestIdAndItemId(guestId: String, itemId: Long): ItemFavorite?
    
    fun existsByUserIdAndItemId(userId: Int, itemId: Long): Boolean
    fun existsByGuestIdAndItemId(guestId: String, itemId: Long): Boolean
    
    fun deleteByUserIdAndItemId(userId: Int, itemId: Long)
    fun deleteByGuestIdAndItemId(guestId: String, itemId: Long)
    
    fun findByUserId(userId: Int): List<ItemFavorite>
    fun findByGuestId(guestId: String): List<ItemFavorite>
}
