package com.example.myapp.dto.market

data class CategoryResponse(
    val categoryId: Long,
    val categoryName: String,
    val parentId: Long?,
    val level: Int,
    val iconUrl: String?,
    val sortOrder: Int,
    val children: List<CategoryResponse> = emptyList()
)
