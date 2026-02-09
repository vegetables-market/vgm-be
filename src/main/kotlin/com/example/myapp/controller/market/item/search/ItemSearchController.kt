package com.example.myapp.controller.market.item.search

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.item.ItemResponse
import com.example.myapp.dto.market.item.search.ItemSearchRequest
import com.example.myapp.dto.market.PaginatedResponse
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.ItemSearchService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 商品検索・一覧取得コントローラー
 * 条件を指定して商品を検索・一覧表示します。
 */
@RestController
@RequestMapping("/v1/market/items")
class ItemSearchController(
    private val itemSearchService: ItemSearchService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService
) {

    /**
     * 商品検索
     */
    @GetMapping("/search")
    fun searchItems(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) minPrice: Int?,
        @RequestParam(required = false) maxPrice: Int?,
        @RequestParam(required = false) condition: Int?,
        @RequestParam(defaultValue = "newest") sort: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        servletRequest: HttpServletRequest
    ): ResponseEntity<PaginatedResponse<ItemResponse>> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)

        val request = ItemSearchRequest(
            q = q,
            categoryId = categoryId,
            minPrice = minPrice,
            maxPrice = maxPrice,
            condition = condition,
            sort = sort,
            page = page,
            limit = limit
        )

        val result = itemSearchService.searchItems(request, userId)
        return ResponseEntity.ok(result)
    }
}
