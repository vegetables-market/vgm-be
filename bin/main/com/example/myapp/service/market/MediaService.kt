package com.example.myapp.service.market

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.fasterxml.jackson.databind.ObjectMapper

@Service
class MediaService(
    @Value("\${vgm.media.jwt-secret}") private val jwtSecret: String,
    private val objectMapper: ObjectMapper
) {

    fun generateUploadToken(userId: Int, role: String, preferredFilename: String? = null): UploadTokenResponse {
        // ファイル名の決定: ADMINかつ希望があればそれを使用、それ以外はUUID
        val filename = if (role == "ADMIN" && !preferredFilename.isNullOrBlank()) {
            preferredFilename
        } else {
            UUID.randomUUID().toString()
        }

        // JWT Header
        val headerMap = mapOf("alg" to "HS256", "typ" to "JWT")
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(headerMap)
        )

        // JWT Payload
        val now = System.currentTimeMillis() / 1000
        val exp = now + 600 // 10分有効
        val payloadMap = mapOf(
            "sub" to userId.toString(),
            "filename" to filename, // 許可されたファイル名
            "role" to role,
            "exp" to exp,
            "action" to "upload"
        )
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(payloadMap)
        )

        // Signature
        val signatureInput = "$header.$payload"
        val signature = signHmacSha256(signatureInput, jwtSecret)

        val token = "$signatureInput.$signature"

        return UploadTokenResponse(
            token = token,
            filename = filename,
            expiresAt = exp
        )
    }

    private fun signHmacSha256(data: String, secret: String): String {
        val algorithm = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKeySpec)
        val signatureBytes = mac.doFinal(data.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
    }
}

data class UploadTokenResponse(
    val token: String,
    val filename: String,
    val expiresAt: Long
)
