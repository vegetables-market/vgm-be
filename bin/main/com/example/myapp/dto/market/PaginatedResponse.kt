package com.example.myapp.dto.market

data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)
