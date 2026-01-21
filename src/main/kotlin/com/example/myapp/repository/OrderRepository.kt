package com.example.myapp.repository

import com.example.myapp.entity.Order
import com.example.myapp.entity.OrderStatus
import com.example.myapp.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByBuyer(buyer: User): List<Order>
    fun findBySeller(seller: User): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByBuyerAndStatus(buyer: User, status: OrderStatus): List<Order>
    fun findBySellerAndStatus(seller: User, status: OrderStatus): List<Order>
    fun findByBuyerOrderByCreatedAtDesc(buyer: User): List<Order>
    fun findBySellerOrderByCreatedAtDesc(seller: User): List<Order>
}
