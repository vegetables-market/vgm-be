package com.example.myapp.service.market.item

import com.example.myapp.dto.market.item.liisting.CreateItemRequest
import com.example.myapp.dto.market.item.SimpleItemResponse
import com.example.myapp.entity.market.item.Item
import com.example.myapp.entity.market.item.ItemImage
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.market.item.ItemImageRepository
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.user.UserRepository
import com.example.myapp.repository.user.address.UserAddressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import jakarta.persistence.EntityManager

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager,
    private val userAddressRepository: UserAddressRepository,
) {
    @Transactional
    fun createDraft(userId: Int): Item {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        
        val draftItem = Item(
            user = user,
            status = 0,
            quantity = 1,
            shippingPayerType = 0,
            shippingMethodId = null,
            shippingDaysId = null,
            shippingOriginArea = null,
            categoryId = null, 
            name = null,
            price = null,
            description = null
        )
        return itemRepository.save(draftItem)
    }

    // 修正: 戻り値を SimpleItemResponse に指定
    @Transactional
    fun linkImages(userId: Int, displayId: String, filenames: List<String>): SimpleItemResponse {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        val itemId = item.itemId!!
        if (item.user.userId != userId) throw RuntimeException("Not authorized")

        val currentMaxOrder = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId).maxOfOrNull { it.displayOrder } ?: 0
        
        filenames.forEachIndexed { index, filename ->
            val itemImage = ItemImage(
                itemId = itemId,
                imageUrl = filename,
                displayOrder = currentMaxOrder + index + 1
            )
            itemImageRepository.save(itemImage)
        }
        itemRepository.flush()
        entityManager.clear()

        val finalItem = itemRepository.findById(itemId).orElseThrow()
        return toSimpleResponse(finalItem)
    }

    @Transactional
    fun publishItem(userId: Int, displayId: String, request: CreateItemRequest): SimpleItemResponse {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        val itemId = item.itemId!!
        if (item.user.userId != userId) throw RuntimeException("Not authorized")
        if (request.categoryId <= 0) throw RuntimeException("Invalid categoryId: ${request.categoryId}")

        println("[DEBUG] publishItem called: displayId=$displayId, itemId=$itemId")
        println("[DEBUG] request.imageUrls = ${request.imageUrls}")

        // 更新
        item.name = request.name
        item.description = request.description
        item.categoryId = request.categoryId
        item.price = request.price
        item.quantity = request.quantity
        item.status = 2 // 出品中
        item.shippingPayerType = request.shippingPayerType
        item.shippingOriginArea = request.shippingOriginArea
        item.shippingOriginAddressId = resolveShippingOriginAddressId(userId, request.shippingOriginAddressId)
        item.shippingDaysId = request.shippingDaysId
        item.shippingMethodId = request.shippingMethodId
        item.itemCondition = request.itemCondition
        item.updatedAt = LocalDateTime.now()
        
        itemRepository.save(item)

        // 画像の更新 (deleteAllByItemId を使用)
        val existingImages = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId)
        println("[DEBUG] 既存画像数 (削除前): ${existingImages.size}")
        itemImageRepository.deleteAllByItemId(itemId)
        itemImageRepository.flush()
        println("[DEBUG] deleteAllByItemId 完了")

        request.imageUrls?.forEachIndexed { index, url ->
            println("[DEBUG] 画像保存: index=$index, url=$url, itemId=$itemId")
            val itemImage = ItemImage(
                itemId = itemId,
                imageUrl = url,
                displayOrder = index
            )
            itemImageRepository.save(itemImage)
        }

        // キャッシュ対策を追加
        itemImageRepository.flush()
        entityManager.clear()
        
        val savedImages = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId)
        println("[DEBUG] 保存後の画像数: ${savedImages.size}")
        savedImages.forEach { println("[DEBUG]   imageId=${it.imageId}, url=${it.imageUrl}") }

        val finalItem = itemRepository.findById(itemId).orElseThrow()
        return toSimpleResponse(finalItem)
    }

    @Transactional
    fun createItem(userId: Int, request: CreateItemRequest): SimpleItemResponse {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }

        val item = Item(
            user = user,
            name = request.name,
            description = request.description,
            categoryId = request.categoryId,
            price = request.price,
            quantity = request.quantity,
            status = 1.toShort(),
            shippingPayerType = request.shippingPayerType,
            shippingOriginArea = request.shippingOriginArea,
            shippingOriginAddressId = resolveShippingOriginAddressId(userId, request.shippingOriginAddressId),
            shippingDaysId = request.shippingDaysId,
            shippingMethodId = request.shippingMethodId,
            itemCondition = request.itemCondition
        )
        
        val savedItem = itemRepository.save(item)
        val savedItemId = savedItem.itemId ?: throw RuntimeException("Failed to save item")
        
        request.imageUrls?.forEachIndexed { index, url ->
            val itemImage = ItemImage(
                itemId = savedItemId,
                imageUrl = url,
                displayOrder = index + 1
            )
            itemImageRepository.save(itemImage)
        }
        
        return toSimpleResponse(savedItem)
    }

    fun getMyItems(userId: Int): List<SimpleItemResponse> {
        val visibleStatuses = listOf<Short>(2, 3, 4, 5)
        val items = itemRepository.findByUser_UserIdAndStatusInOrderByCreatedAtDesc(userId, visibleStatuses)
        return items.map { toSimpleResponse(it) }
    }
    
    private fun toSimpleResponse(item: Item): SimpleItemResponse {
        // メソッド名を存在する Asc ありの方に変更
        val imageUrl = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(item.itemId!!)
            .firstOrNull()?.imageUrl
        
        return SimpleItemResponse(
            id = item.displayId,
            name = item.name ?: "",
            price = item.price ?: 0,
            status = item.status.toInt(),
            imageUrl = imageUrl,
            createdAt = item.createdAt.toString()
        )
    }

    @Transactional
    fun deleteItem(userId: Int, displayId: String) {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        if (item.user.userId != userId) throw RuntimeException("Not authorized")
        
        item.status = 6 
        item.updatedAt = LocalDateTime.now()
        itemRepository.save(item)
    }

    @Transactional
    fun updateItemStatus(userId: Int, displayId: String, newStatus: Int) {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        if (item.user.userId != userId) throw RuntimeException("Not authorized")
        
        when (newStatus) {
            2 -> item.status = 2 
            5 -> item.status = 5 
            else -> throw RuntimeException("Invalid status transition")
        }
        
        item.updatedAt = LocalDateTime.now()
        itemRepository.save(item)
    }
    private fun resolveShippingOriginAddressId(userId: Int, addressId: Int?): Int? {
        if (addressId == null) return null
        val address = userAddressRepository.findByAddressIdAndUserIdAndDeletedAtIsNull(addressId, userId)
            ?: throw AppException(ErrorCode.INVALID_INPUT, "shippingOriginAddressId is invalid")
        return address.addressId
    }
}
