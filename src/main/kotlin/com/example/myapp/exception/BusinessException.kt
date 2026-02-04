package com.example.myapp.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val errorCode: String,
    override val message: String,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    val details: List<String> = emptyList()
) : RuntimeException(message)
