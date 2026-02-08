package com.example.myapp.dto.user.profile

/**
 * 自己紹介更新リクエストDTO
 *
 * @property bio 新しい自己紹介文
 * Used in: [com.example.myapp.controller.user.profile.UpdateBioController]
 */

data class UpdateBioRequest(
    val bio: String
)
