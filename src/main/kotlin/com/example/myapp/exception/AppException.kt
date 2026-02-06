package com.example.myapp.exception

open class AppException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
    val details: List<String>? = null
) : RuntimeException(message)
