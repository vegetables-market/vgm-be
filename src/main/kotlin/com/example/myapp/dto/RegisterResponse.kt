package com.example.myapp.dto

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Long? = null,
    val username: String? = null,
<<<<<<< HEAD
    val theme: Int? = null
=======
    val theme: String? = null
>>>>>>> 98a3824ee00446e97cfcc4f58bfe5960549fbdcb
)
