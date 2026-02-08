package com.example.myapp.security

import com.example.myapp.dto.common.ErrorResponse
import com.example.myapp.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 未認証アクセスハンドラ
 *
 * 未ログインユーザーが保護されたリソースにアクセスした際（401 Unauthorized）、
 * 共通のエラーレスポンス形式（JSON）で応答を返します。
 * Spring SecurityのデフォルトのHTMLレスポンスではなく、API向けのJSONエラーを返すために使用します。
 */
@Component
class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        // Determine specific error code if possible, otherwise default to AUTH_REQUIRED
        // Note: Detailed "BadCredentials" usually caught by ExceptionHandler if thrown during processing,
        // but EntryPoint catches cases where filter chain rejects request before controller.
        val errorCode = ErrorCode.AUTH_REQUIRED

        val errorResponse = ErrorResponse(
            errorCode = errorCode.code,
            message = errorCode.message,
            details = listOf(authException.message ?: "Authentication failed"),
            timestamp = LocalDateTime.now()
        )

        objectMapper.writeValue(response.writer, errorResponse)
    }
}
