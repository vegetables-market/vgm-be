package com.example.myapp.service.market.cart

import com.example.myapp.dto.market.cart.CartItemResponse
import com.example.myapp.dto.market.cart.CartResponse
import com.example.myapp.repository.market.cart.CartItemRepository
import com.example.myapp.repository.market.item.ItemImageRepository
import com.example.myapp.repository.market.item.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * カート取得ユースケース
 */
@Service
class GetCart(
    private val cartItemRepository: CartItemRepository,
    private val itemRepository: ItemRepository,
    private val itemImageRepository: ItemImageRepository
) {

    @Transactional(readOnly = true)
    operator fun invoke(userId: Int?, guestId: String?): CartResponse {
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
                    itemId = item.displayId,
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
}
