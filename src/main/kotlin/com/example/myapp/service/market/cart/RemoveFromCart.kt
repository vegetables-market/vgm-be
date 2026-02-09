package com.example.myapp.service.market.cart

import com.example.myapp.repository.market.cart.CartItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * カート削除ユースケース
 */
@Service
class RemoveFromCart(
    private val cartItemRepository: CartItemRepository
) {

    @Transactional
    operator fun invoke(cartItemId: Long, userId: Int?, guestId: String?) {
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
