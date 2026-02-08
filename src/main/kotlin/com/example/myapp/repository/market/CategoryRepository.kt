package com.example.myapp.repository.market

import com.example.myapp.entity.market.category.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByParentIdIsNullOrderBySortOrderAsc(): List<Category>
    fun findByParentIdOrderBySortOrderAsc(parentId: Long): List<Category>
}
