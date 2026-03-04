package com.example.myapp.config

import com.example.myapp.dto.common.ErrorResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * アプリケーション独自の例外 (AppException)
     */
    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ResponseEntity<ErrorResponse> {
        logger.warn(
            "AppException handled: code={}, message={}, details={}",
            ex.errorCode.code,
            ex.message,
            ex.details,
        )
        return buildResponse(ex.errorCode, ex.details)
    }

    /**
     * Spring Security: 認証失敗 (BadCredentialsException)
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return buildResponse(ErrorCode.AUTH_INVALID_CREDENTIALS)
    }

    /**
     * Spring Security: アカウントロックなど
     */
    @ExceptionHandler(LockedException::class, DisabledException::class)
    fun handleAccountStatusException(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        return buildResponse(ErrorCode.AUTH_ACCOUNT_LOCKED)
    }

    /**
     * Spring Security: 認可失敗 (AccessDeniedException)
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return buildResponse(ErrorCode.AUTH_FORBIDDEN)
    }

    /**
     * Static resource not found
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, listOf(ex.message ?: "Resource not found"))
    }

    /**
     * JSONパース失敗（入力形式不正）
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return buildResponse(ErrorCode.INVALID_INPUT, listOf(ex.message ?: "Invalid request body"))
    }

    /**
     * その他の例外
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return buildResponse(ErrorCode.SYSTEM_ERROR, listOf(ex.message ?: "Unknown error"))
    }

    private fun buildResponse(errorCode: ErrorCode, details: List<String>? = null): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            errorCode = errorCode.code,
            message = errorCode.message,
            details = details ?: emptyList(),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }
}
