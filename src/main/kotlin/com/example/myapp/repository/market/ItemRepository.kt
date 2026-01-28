package com.example.myapp.repository.market

import com.example.myapp.entity.market.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {
    fun findByUser_UserIdOrderByCreatedAtDesc(userId: Int): List<Item>
}
