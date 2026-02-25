package com.example.myapp.service.market.cart

import com.example.myapp.entity.market.cart.CartItem
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
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
    operator fun invoke(userId: Int?, guestId: String?, displayId: String, quantity: Int): Long {
        if (userId == null && guestId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "User ID or Guest ID must be provided")
        }
        if (quantity <= 0) {
            throw AppException(ErrorCode.INVALID_INPUT, "Quantity must be greater than 0")
        }

        // Check if item exists
        val item = itemRepository.findByDisplayId(displayId)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Item not found")
        val itemId = item.itemId!!
        if (item.status.toInt() != 2) {
            throw AppException(ErrorCode.INVALID_INPUT, "Item is not available for sale")
        }
        if (item.quantity <= 0) {
            throw AppException(ErrorCode.ITEM_OUT_OF_STOCK, "Item is out of stock")
        }
        
        // Check if already in cart
        val existingItem = if (userId != null) {
            cartItemRepository.findByUserIdAndItemId(userId, itemId)
        } else {
            cartItemRepository.findByGuestIdAndItemId(guestId!!, itemId)
        }

        if (existingItem != null) {
            val updatedQuantity = existingItem.quantity + quantity
            if (updatedQuantity > item.quantity) {
                throw AppException(
                    ErrorCode.ITEM_OUT_OF_STOCK,
                    "Requested quantity exceeds available stock (${item.quantity})",
                )
            }
            existingItem.quantity = updatedQuantity
            cartItemRepository.save(existingItem)
            return existingItem.cartItemId
        } else {
            if (quantity > item.quantity) {
                throw AppException(
                    ErrorCode.ITEM_OUT_OF_STOCK,
                    "Requested quantity exceeds available stock (${item.quantity})",
                )
            }
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
