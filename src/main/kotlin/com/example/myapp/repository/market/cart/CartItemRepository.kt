package com.example.myapp.repository.market.cart

import com.example.myapp.entity.market.cart.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByUserId(userId: Int): List<CartItem>
    fun findByGuestId(guestId: String): List<CartItem>

    fun findByUserIdAndItemId(userId: Int, itemId: Long): CartItem?
    fun findByGuestIdAndItemId(guestId: String, itemId: Long): CartItem?
    fun deleteByUserIdAndItemIdIn(userId: Int, itemIds: List<Long>)
}
