package com.example.myapp.service.market.item

import com.example.myapp.dto.market.item.liisting.CreateItemRequest
import com.example.myapp.dto.market.item.SimpleItemResponse
import com.example.myapp.entity.market.item.Item
import com.example.myapp.entity.market.item.ItemImage
import com.example.myapp.repository.market.item.ItemImageRepository
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val itemImageRepository: ItemImageRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createDraft(userId: Int): Item {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        
        // 空のDraftアイテムを作成 status=0
        val draftItem = Item(
            user = user,
            status = 0,
            quantity = 1,
            // デフォルト値
            shippingPayerType = 0,
            // マスタ未投入環境でもDraft作成が失敗しないよう、配送系はnullで開始
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

    @Transactional
    fun linkImages(
        userId: Int,
        displayId: String,
        filenames: List<String>,
        replaceExisting: Boolean = false
    ) {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        val itemId = item.itemId!!
        if (item.user.userId != userId) throw RuntimeException("Not authorized")

        val currentMaxOrder = if (replaceExisting) {
            itemImageRepository.deleteByItemId(itemId)
            0
        } else {
            itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId)
                .maxOfOrNull { it.displayOrder } ?: 0
        }

        filenames.forEachIndexed { index, filename ->
            val itemImage = ItemImage(
                itemId = itemId,
                imageUrl = filename, // Direct Uploadされたファイル名
                displayOrder = currentMaxOrder + index + 1
            )
            itemImageRepository.save(itemImage)
        }
    }

    @Transactional
    fun publishItem(userId: Int, displayId: String, request: CreateItemRequest): SimpleItemResponse {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        val itemId = item.itemId!!
        if (item.user.userId != userId) throw RuntimeException("Not authorized")
        if (request.categoryId <= 0) throw RuntimeException("Invalid categoryId: ${request.categoryId}")

        // 必須チェック (name, price, etc)
        // ここでバリデーションを行っても良い
        
        // 更新
        item.name = request.name
        item.description = request.description
        item.categoryId = request.categoryId
        item.price = request.price
        item.quantity = request.quantity
        item.status = 2 // 出品中
        item.shippingPayerType = request.shippingPayerType
        item.shippingOriginArea = request.shippingOriginArea
        item.shippingDaysId = request.shippingDaysId
        item.shippingMethodId = request.shippingMethodId
        item.itemCondition = request.itemCondition
        item.updatedAt = LocalDateTime.now()
        
        val savedItem = itemRepository.save(item)
        // imageUrls が指定された場合は、既存画像を置換する。
        // 編集画面で「削除した画像が残る」問題を避けるため、更新APIで最終状態を確定させる。
        request.imageUrls?.let { urls ->
            itemImageRepository.deleteByItemId(itemId)
            urls.forEachIndexed { index, url ->
                itemImageRepository.save(
                    ItemImage(
                        itemId = itemId,
                        imageUrl = url,
                        displayOrder = index + 1
                    )
                )
            }
        }
        
        return toSimpleResponse(savedItem)
    }

    // 既存のcreateItemも残すが、内部実体はDraft->Publishフローにするか、
    // あるいはレガシーとして維持しつつnullable対応だけするか。
    // ここではLegacy維持+nullable対応
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
            status = 1.toShort(), // 出品中
            shippingPayerType = request.shippingPayerType,
            shippingOriginArea = request.shippingOriginArea,
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
        // 出品中(2)、取引中(3)、売切(4)、停止(5)のみ表示（ドラフト・削除済みは除外）
        val visibleStatuses = listOf<Short>(2, 3, 4, 5)
        val items = itemRepository.findByUser_UserIdAndStatusInOrderByCreatedAtDesc(userId, visibleStatuses)
        return items.map { toSimpleResponse(it) }
    }
    
    private fun toSimpleResponse(item: Item): SimpleItemResponse {
        val imageUrl = itemImageRepository.findByItemIdOrderByDisplayOrder(item.itemId!!)
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
        
        // ソフトデリート: ステータスを削除済みに変更
        item.status = 6 // DELETED
        item.updatedAt = LocalDateTime.now()
        itemRepository.save(item)
    }

    @Transactional
    fun updateItemStatus(userId: Int, displayId: String, newStatus: Int) {
        val item = itemRepository.findByDisplayId(displayId) ?: throw RuntimeException("Item not found")
        if (item.user.userId != userId) throw RuntimeException("Not authorized")
        
        // ステータス変更（出品中⇔停止中など）
        // 許可されたステータス遷移のみ実行
        when (newStatus) {
            2 -> item.status = 2 // ON_SALE (出品中)
            5 -> item.status = 5 // SUSPENDED (停止中)
            else -> throw RuntimeException("Invalid status transition")
        }
        
        item.updatedAt = LocalDateTime.now()
        itemRepository.save(item)
    }
}
