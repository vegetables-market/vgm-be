package com.example.myapp.controller.market

import com.example.myapp.dto.market.ItemSearchRequest
import com.example.myapp.dto.market.PaginatedResponse
import com.example.myapp.dto.market.ItemResponse
import com.example.myapp.repository.auth.UserSessionRepository
import com.example.myapp.service.market.ItemSearchService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/market/items")
class ItemSearchController(
    private val itemSearchService: ItemSearchService,
    private val userSessionRepository: UserSessionRepository
) {

    private fun getUserIdFromSession(request: HttpServletRequest): Int? {
        val sessionKey = request.cookies?.find { it.name == "vgm_session" }?.value
            ?: return null

        val session = userSessionRepository.findBySessionKeyAndIsRevokedFalseAndExpiresAtAfter(
            sessionKey,
            LocalDateTime.now()
        ) ?: return null

        return session.userId
    }

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
        val userId = getUserIdFromSession(servletRequest)

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
