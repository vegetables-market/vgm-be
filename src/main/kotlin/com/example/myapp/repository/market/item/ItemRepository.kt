package com.example.myapp.repository.market.item

import com.example.myapp.entity.market.item.Item
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {
    fun findByUser_UserIdOrderByCreatedAtDesc(userId: Int): List<Item>

    fun findByDisplayId(displayId: String): Item?

    // 指定したステータスのアイテムのみ取得（出品中、取引中、売切、停止）
    fun findByUser_UserIdAndStatusInOrderByCreatedAtDesc(userId: Int, statuses: List<Short>): List<Item>
    
    // ステータスで検索
    fun findByStatusOrderByCreatedAtDesc(status: Short, pageable: Pageable): Page<Item>
    
    // カテゴリーで検索
    fun findByCategoryIdAndStatusOrderByCreatedAtDesc(categoryId: Long, status: Short, pageable: Pageable): Page<Item>
    
    // 価格範囲で検索
    fun findByStatusAndPriceBetweenOrderByCreatedAtDesc(status: Short, minPrice: Int, maxPrice: Int, pageable: Pageable): Page<Item>
    
    // 全文検索（タイトル）
    @Query("SELECT i FROM Item i WHERE i.status = :status AND LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY i.createdAt DESC")
    fun searchByKeyword(@Param("keyword") keyword: String, @Param("status") status: Short, pageable: Pageable): Page<Item>
    
    // 複合検索（キーワード + カテゴリー）
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.categoryId = :categoryId AND LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY i.createdAt DESC")
    fun searchByKeywordAndCategory(@Param("keyword") keyword: String, @Param("categoryId") categoryId: Long, @Param("status") status: Short, pageable: Pageable): Page<Item>
    
    // 複合検索（キーワード + 価格範囲）
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.price BETWEEN :minPrice AND :maxPrice AND LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY i.createdAt DESC")
    fun searchByKeywordAndPriceRange(@Param("keyword") keyword: String, @Param("minPrice") minPrice: Int, @Param("maxPrice") maxPrice: Int, @Param("status") status: Short, pageable: Pageable): Page<Item>
    
    // 人気順（お気に入り数）
    fun findByStatusOrderByLikesCountDescCreatedAtDesc(status: Short, pageable: Pageable): Page<Item>
    
    // 価格昇順
    fun findByStatusOrderByPriceAscCreatedAtDesc(status: Short, pageable: Pageable): Page<Item>
    
    // 価格降順
    fun findByStatusOrderByPriceDescCreatedAtDesc(status: Short, pageable: Pageable): Page<Item>
    
    // 関連商品取得（同じカテゴリー）
    fun findByCategoryIdAndStatusAndItemIdNotOrderByCreatedAtDesc(categoryId: Long, status: Short, excludeItemId: Long, pageable: Pageable): Page<Item>
    
    // 出品者の他の商品
    fun findByUser_UserIdAndStatusAndItemIdNotOrderByCreatedAtDesc(userId: Int, status: Short, excludeItemId: Long, pageable: Pageable): Page<Item>
}
