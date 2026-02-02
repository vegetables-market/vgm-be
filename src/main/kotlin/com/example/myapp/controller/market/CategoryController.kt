package com.example.myapp.controller.market

import com.example.myapp.dto.market.CategoryResponse
import com.example.myapp.service.market.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * カテゴリー一覧取得（階層構造）
     */
    @GetMapping
    fun getAllCategories(): ResponseEntity<Map<String, List<CategoryResponse>>> {
        val categories = categoryService.getAllCategories()
        return ResponseEntity.ok(mapOf("categories" to categories))
    }
}
