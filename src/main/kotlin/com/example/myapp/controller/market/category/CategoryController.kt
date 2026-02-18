package com.example.myapp.controller.market.category

import com.example.myapp.dto.market.CategoryResponse
import com.example.myapp.service.market.category.GetAllCategories
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * カテゴリーコントローラー
 */
@RestController
@RequestMapping("/v1/market/categories")
class CategoryController(
    private val getAllCategories: GetAllCategories
) {

    @GetMapping
    fun getCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = getAllCategories()
        return ResponseEntity.ok(categories)
    }
}
