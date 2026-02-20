package com.example.myapp.entity.auth

<<<<<<< HEAD

=======
>>>>>>> 98a3824ee00446e97cfcc4f58bfe5960549fbdcb
/**
 * 簡易なユーザーセッション DTO。
 * 実行時には認証フィルターなどで principal にセットされる想定です。
 */
data class UserSession(
    val userId: Long,
<<<<<<< HEAD
    val username: String? = null,
    val theme: Int? = null
)

=======
    val username: String? = null
)
>>>>>>> 98a3824ee00446e97cfcc4f58bfe5960549fbdcb
