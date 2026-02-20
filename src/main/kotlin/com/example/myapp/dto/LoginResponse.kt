package com.example.myapp.dto

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val requireTotp: Boolean = false,
    val userId: Long? = null,
    val username: String? = null,
    val email: String? = null,
<<<<<<< HEAD
    val theme: Int? = null
=======
    val theme: String? = null
>>>>>>> 98a3824ee00446e97cfcc4f58bfe5960549fbdcb
)
