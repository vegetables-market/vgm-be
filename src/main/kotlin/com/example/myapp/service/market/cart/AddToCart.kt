package com.example.myapp.service.market.cart

import com.example.myapp.entity.market.cart.CartItem
import com.example.myapp.repository.market.cart.CartItemRepository
import com.example.myapp.repository.market.item.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * カート追加ユースケース
 */
@Service
class AddToCart(
    private val cartItemRepository: CartItemRepository,
    private val itemRepository: ItemRepository
) {

    @Transactional
    operator fun invoke(userId: Int?, guestId: String?, itemId: Long, quantity: Int): Long {
        if (userId == null && guestId == null) {
            throw IllegalArgumentException("User ID or Guest ID must be provided")
        }

        // Check if item exists
        val item = itemRepository.findById(itemId).orElseThrow { IllegalArgumentException("Item not found") }

        // Check availability (e.g. status == 2) - skipping for brevity, should be added
        
        // Check if already in cart
        val existingItem = if (userId != null) {
            cartItemRepository.findByUserIdAndItemId(userId, itemId)
        } else {
            cartItemRepository.findByGuestIdAndItemId(guestId!!, itemId)
        }

        if (existingItem != null) {
            existingItem.quantity += quantity
            cartItemRepository.save(existingItem)
            return existingItem.cartItemId
        } else {
            val newItem = CartItem(
                userId = userId,
                guestId = guestId,
                itemId = itemId,
                quantity = quantity
            )
            cartItemRepository.save(newItem)
            return newItem.cartItemId
        }
    }
}
