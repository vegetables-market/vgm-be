package com.example.myapp.repository.market

import com.example.myapp.entity.market.item.ItemImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemImageRepository : JpaRepository<ItemImage, Long> {
    fun findByItemIdOrderByDisplayOrderAsc(itemId: Long): List<ItemImage>
    fun findByItemIdOrderByDisplayOrder(itemId: Long): List<ItemImage>
}
