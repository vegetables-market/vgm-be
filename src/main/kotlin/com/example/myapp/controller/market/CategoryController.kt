package com.example.myapp.controller.market

import com.example.myapp.entity.market.Category
import com.example.myapp.repository.market.CategoryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/items/categories")
class CategoryController(
    private val categoryRepository: CategoryRepository
) {

    @GetMapping
    fun getCategories(): ResponseEntity<List<Category>> {
        val categories = categoryRepository.findByParentIdIsNullOrderBySortOrderAsc()
        return ResponseEntity.ok(categories)
    }
}
