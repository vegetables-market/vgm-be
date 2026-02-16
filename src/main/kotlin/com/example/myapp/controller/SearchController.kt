package com.example.myapp.controller

import org.springframework.web.bind.annotation.*
import com.example.myapp.service.ItemSearch
import com.example.myapp.model.ItemSearchResult

@RestController
@RequestMapping("/api/items")
class SearchController(
    private val itemSearch: ItemSearch
) {

    @GetMapping("/search")
    fun search(
        @RequestParam keyword: String
    ): List<ItemSearchResult> {
        val synonyms = listOf("関連語1", "関連語2")
        return itemSearch.search(keyword, synonyms)
    }
}