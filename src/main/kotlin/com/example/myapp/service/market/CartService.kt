package com.example.myapp.service.market

import com.example.myapp.dto.market.CartItemResponse
import com.example.myapp.dto.market.CartResponse
import com.example.myapp.entity.market.CartItem
import com.example.myapp.repository.market.CartItemRepository
import com.example.myapp.repository.market.ItemImageRepository
import com.example.myapp.repository.market.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val itemRepository: ItemRepository,
    private val itemImageRepository: ItemImageRepository
) {

    @Transactional(readOnly = true)
    fun getCart(userId: Int?, guestId: String?): CartResponse {
        val cartItems = when {
            userId != null -> cartItemRepository.findByUserId(userId)
            guestId != null -> cartItemRepository.findByGuestId(guestId)
            else -> emptyList()
        }

        if (cartItems.isEmpty()) {
            return CartResponse(items = emptyList(), totalAmount = 0L)
        }

        // Map to DTO
        val responseItems = cartItems.map { cartItem ->
            val item = itemRepository.findById(cartItem.itemId).orElse(null)
            if (item != null) {
                // Get thumbnail
                val thumbnail = itemImageRepository.findByItemIdOrderByDisplayOrder(item.itemId!!).firstOrNull()?.imageUrl
                
                CartItemResponse(
                    cartItemId = cartItem.cartItemId,
                    itemId = item.itemId!!,
                    name = item.name ?: "Unknown Item",
                    price = item.price?.toLong() ?: 0L,
                    quantity = cartItem.quantity,
                    subtotal = (item.price?.toLong() ?: 0L) * cartItem.quantity,
                    thumbnailUrl = thumbnail
                )
            } else {
                null
            }
        }.filterNotNull()

        val total = responseItems.sumOf { it.subtotal }

        return CartResponse(
            items = responseItems,
            totalAmount = total
        )
    }

    @Transactional
    fun addToCart(userId: Int?, guestId: String?, itemId: Long, quantity: Int): Long {
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

    @Transactional
    fun updateQuantity(cartItemId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartItemRepository.deleteById(cartItemId)
            return
        }
        
        val cartItem = cartItemRepository.findById(cartItemId).orElseThrow { IllegalArgumentException("Cart item not found") }
        cartItem.quantity = newQuantity
        cartItemRepository.save(cartItem)
    }

    @Transactional
    fun removeFromCart(cartItemId: Long) {
        cartItemRepository.deleteById(cartItemId)
    }
}
