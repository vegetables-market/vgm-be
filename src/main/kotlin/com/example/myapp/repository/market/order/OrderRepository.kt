package com.example.myapp.repository.market.order

import com.example.myapp.entity.market.order.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderIdAndBuyerId(orderId: Long, buyerId: Int): Order?
}

