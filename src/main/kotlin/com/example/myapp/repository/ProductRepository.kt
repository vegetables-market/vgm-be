package com.example.myapp.repository

//import com.example.myapp.entity.market.Product
//import com.example.myapp.entity.market.ProductStatus
//import com.example.myapp.entity.user.User
//import org.springframework.data.jpa.repository.EntityGraph
//import org.springframework.data.jpa.repository.JpaRepository
//import org.springframework.stereotype.Repository
//
//@Repository
//interface ProductRepository : JpaRepository<Product, Long> {
//    fun findByStatus(status: ProductStatus): List<Product>
//    fun findBySeller(seller: User): List<Product>
//    fun findBySellerAndStatus(seller: User, status: ProductStatus): List<Product>
//
//    @EntityGraph(attributePaths = ["seller"])
//    fun findByStatusOrderByCreatedAtDesc(status: ProductStatus): List<Product>
//}
