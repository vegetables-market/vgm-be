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
    fun createDraft(userId: Int): Item {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        
        // 空のDraftアイテムを作成 status=0
        val draftItem = Item(
            user = user,
            status = 0,
            quantity = 1,
            // デフォルト値
            shippingPayerType = 0,
            shippingMethodId = 1, // 仮
            shippingDaysId = 1, // 仮
            shippingOriginArea = 1, // 仮
            categoryId = null, 
            name = null,
            price = null,
            description = null
        )
        return itemRepository.save(draftItem)
    }

    @Transactional
    fun linkImages(userId: Int, itemId: Long, filenames: List<String>) {
        val item = itemRepository.findById(itemId).orElseThrow { RuntimeException("Item not found") }
        if (item.user.userId != userId) throw RuntimeException("Not authorized")

        // 既存画像をクリアするか、追加するか。ここでは「追加」とするか、
        // 「Draftへの画像追加」はCreate時の一回とみなすか。
        // シンプルに「現在のリストで上書き」または「追加」。
        // ユースケース: createDraft -> upload -> linkImages
        
        // 既存の画像を削除せずに追加する実装にする
        val currentMaxOrder = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId).maxOfOrNull { it.displayOrder } ?: 0
        
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
    fun publishItem(userId: Int, itemId: Long, request: CreateItemRequest): ItemResponse {
        val item = itemRepository.findById(itemId).orElseThrow { RuntimeException("Item not found") }
        if (item.user.userId != userId) throw RuntimeException("Not authorized")

        // 必須チェック (name, price, etc)
        // ここでバリデーションを行っても良い
        
        // 更新
        val updatedItem = item.copy(
            name = request.name,
            description = request.description,
            categoryId = request.categoryId,
            price = request.price,
            quantity = request.quantity,
            status = 2, // 出品中
            shippingPayerType = request.shippingPayerType,
            shippingOriginArea = request.shippingOriginArea,
            shippingDaysId = request.shippingDaysId,
            shippingMethodId = request.shippingMethodId,
            itemCondition = request.itemCondition,
            updatedAt = java.time.LocalDateTime.now()
        )
        val savedItem = itemRepository.save(updatedItem)

        // 画像URLリストがリクエストに含まれている場合、
        // もし「LinkImages」で既に紐付いているなら何もしないか、
        // あるいはリクエストのimage_urlsで順序を再設定するなど。
        // 今回のフローでは「Direct Uploadしたfilename」がimage_urlsに入ってくる想定。
        // しかし既にlinkImagesで保存済みかもしれない。
        // シンプルにするため: Draft作成 -> 画像Upload&Link -> 最後にPublish(内容はupdate)
        // Publish時に画像リストは送られてこない（または無視する）方が安全かもだが、
        // Frontendの既存のCreateItemRequestを再利用するなら、そこに含まれるimage_urlsを使って
        // 画像の順序などを整える処理を入れてもいい。
        
        // 今回は「既にlinkImagesで保存されている」前提とし、request.imageUrlsは無視する
        // （または確認用に使う）
        
        return toResponse(savedItem)
    }

    // 既存のcreateItemも残すが、内部実体はDraft->Publishフローにするか、
    // あるいはレガシーとして維持しつつnullable対応だけするか。
    // ここではLegacy維持+nullable対応
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
        
        return toResponse(savedItem)
    }

    fun getMyItems(userId: Int): List<ItemResponse> {
        val items = itemRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
        return items.map { toResponse(it) }
    }
    
    private fun toResponse(item: Item): ItemResponse {
        val itemId = item.itemId!!
        val images = itemImageRepository.findByItemIdOrderByDisplayOrderAsc(itemId)
        val firstImage = images.firstOrNull()?.imageUrl
        
        return ItemResponse(
            id = itemId,
            name = item.name,
            price = item.price,
            status = item.status.toInt(),
            imageUrl = firstImage,
            createdAt = item.createdAt.toString()
        )
    }
}
