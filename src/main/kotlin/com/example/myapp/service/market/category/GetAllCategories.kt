package com.example.myapp.service.market.category

import com.example.myapp.dto.market.CategoryResponse
import com.example.myapp.entity.market.category.Category
import com.example.myapp.repository.market.category.CategoryRepository
import org.springframework.stereotype.Service

/**
 * カテゴリー一覧取得ユースケース
 */
@Service
class GetAllCategories(
    private val categoryRepository: CategoryRepository
) {

    /**
     * カテゴリー一覧取得（階層構造）
     */
    operator fun invoke(): List<CategoryResponse> {
        val allCategories = categoryRepository.findAll()
        
        // ルートカテゴリー（親がnull）を取得
        val rootCategories = allCategories.filter { it.parentId == null }
            .sortedBy { it.sortOrder }

        return rootCategories.map { buildCategoryTree(it, allCategories) }
    }

    /**
     * カテゴリーツリーを構築
     */
    private fun buildCategoryTree(
        category: Category,
        allCategories: List<Category>
    ): CategoryResponse {
        val children = allCategories.filter { it.parentId == category.categoryId }
            .sortedBy { it.sortOrder }
            .map { buildCategoryTree(it, allCategories) }

        return CategoryResponse(
            categoryId = category.categoryId!!,
            categoryName = category.name,
            parentId = category.parentId,
            level = category.level,
            iconUrl = category.iconUrl,
            sortOrder = category.sortOrder,
            children = children
        )
    }
}
