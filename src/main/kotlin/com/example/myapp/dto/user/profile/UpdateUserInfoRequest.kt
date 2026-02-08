package com.example.myapp.dto.user.profile

/**
 * ユーザー情報更新リクエストDTO
 *
 * @property gender 性別 (1: Male, 2: Female, 9: Other)
 * @property birthDate 生年月日 (YYYY-MM-DD)
 * Used in: [com.example.myapp.controller.user.account.UpdateUserInfoController]
 */

data class UpdateUserInfoRequest(
    val gender: Short? = null,
    val birthDate: String? = null  // YYYY-MM-DD format
)
