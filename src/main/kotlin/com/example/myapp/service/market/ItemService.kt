package com.example.myapp.service.market

import com.example.myapp.dto.market.CreateItemRequest
import com.example.myapp.dto.market.ItemResponse
import com.example.myapp.entity.market.Item
import com.example.myapp.entity.market.ItemImage
import com.example.myapp.repository.market.ItemImageRepository
import com.example.myapp.repository.market.ItemRepository
import com.example.myapp.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createItem(userId: Int, request: CreateItemRequest): ItemResponse {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }

        val item = Item(
            user = user,
            name = request.name,
            description = request.description,
            categoryId = request.categoryId,
            price = request.price,
            quantity = request.quantity,
            status = 1, // 出品中
            shippingPayerType = request.shippingPayerType,
            shippingOriginArea = request.shippingOriginArea,
            shippingDaysId = request.shippingDaysId,
            shippingMethodId = request.shippingMethodId,
            itemCondition = request.itemCondition
        )
        
        val savedItem = itemRepository.save(item)
        val savedItemId = savedItem.itemId ?: throw RuntimeException("Failed to save item")
        
        // 画像保存
        request.imageUrls.forEachIndexed { index, url ->
            val itemImage = ItemImage(
                itemId = savedItemId,
                imageUrl = url,
                displayOrder = index + 1
            )
            itemImageRepository.save(itemImage)
        }
        
        return ItemResponse(
            id = savedItemId,
            name = savedItem.name,
            price = savedItem.price,
            status = savedItem.status,
            imageUrl = request.imageUrls.firstOrNull(),
            createdAt = savedItem.createdAt.toString()
        )
    }

    fun getMyItems(userId: Int): List<ItemResponse> {
        val items = itemRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
        return items.map { item ->
            val itemId = item.itemId!!
            // N+1問題になるが、一旦これで実装。必要に応じて@EntityGraphなどで最適化
            val images = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId)
            val firstImage = images.firstOrNull()?.imageUrl
            
            ItemResponse(
                id = itemId,
                name = item.name,
                price = item.price,
                status = item.status,
                imageUrl = firstImage,
                createdAt = item.createdAt.toString()
            )
        }
    }
}
