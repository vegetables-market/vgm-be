package com.example.myapp.config

import com.example.myapp.dto.ErrorResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * アプリケーション独自の例外 (AppException)
     */
    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ResponseEntity<ErrorResponse> {
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
     * ビジネス例外 (BusinessException)
     */
    @ExceptionHandler(com.example.myapp.exception.BusinessException::class)
    fun handleBusinessException(ex: com.example.myapp.exception.BusinessException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            errorCode = ex.errorCode,
            message = ex.message,
            details = ex.details,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(ex.httpStatus).body(response)
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
