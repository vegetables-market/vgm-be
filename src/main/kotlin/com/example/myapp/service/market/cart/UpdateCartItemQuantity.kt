package com.example.myapp.service.market.cart

import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.market.cart.CartItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * カート数量更新ユースケース
 */
@Service
class UpdateCartItemQuantity(
    private val cartItemRepository: CartItemRepository,
    private val itemRepository: ItemRepository
) {

    @Transactional
    operator fun invoke(cartItemId: Long, newQuantity: Int, userId: Int?, guestId: String?) {
        if (newQuantity <= 0) {
            // 数量が0以下の場合は削除
            removeFromCart(cartItemId, userId, guestId)
            return
        }
        
        val cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow { com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.RESOURCE_NOT_FOUND, "Cart item not found") }

        // Ownership check
        if (userId != null) {
            if (cartItem.userId != userId) {
                 throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FORBIDDEN, "Not authorized to update this cart item")
            }
        } else if (guestId != null) {
            if (cartItem.guestId != guestId) {
                 throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FORBIDDEN, "Not authorized to update this cart item")
            }
        } else {
             throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_REQUIRED, "User not authenticated")
        }

        val item = itemRepository.findById(cartItem.itemId)
            .orElseThrow { AppException(ErrorCode.RESOURCE_NOT_FOUND, "Item not found") }

        if (item.status.toInt() != 2) {
            throw AppException(ErrorCode.INVALID_INPUT, "Item is not available for sale")
        }
        if (newQuantity > item.quantity) {
            throw AppException(
                ErrorCode.ITEM_OUT_OF_STOCK,
                "Requested quantity exceeds available stock (${item.quantity})",
            )
        }

        cartItem.quantity = newQuantity
        cartItemRepository.save(cartItem)
    }

    private fun removeFromCart(cartItemId: Long, userId: Int?, guestId: String?) {
        val cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow { com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.RESOURCE_NOT_FOUND, "Cart item not found") }

        // Ownership check
        if (userId != null) {
            if (cartItem.userId != userId) {
                 throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FORBIDDEN, "Not authorized to remove this cart item")
            }
        } else if (guestId != null) {
            if (cartItem.guestId != guestId) {
                 throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_FORBIDDEN, "Not authorized to remove this cart item")
            }
        } else {
            throw com.example.myapp.exception.AppException(com.example.myapp.exception.ErrorCode.AUTH_REQUIRED, "User not authenticated")
        }

        cartItemRepository.deleteById(cartItemId)
    }
}
