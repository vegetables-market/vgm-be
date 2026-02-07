package com.example.myapp.security

import com.example.myapp.dto.common.ErrorResponse
import com.example.myapp.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RestAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorCode = ErrorCode.AUTH_FORBIDDEN

        val errorResponse = ErrorResponse(
            errorCode = errorCode.code,
            message = errorCode.message,
            details = listOf(accessDeniedException.message ?: "Access denied"),
            timestamp = LocalDateTime.now()
        )

        objectMapper.writeValue(response.writer, errorResponse)
    }
}
