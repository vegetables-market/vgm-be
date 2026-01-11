package com.example.myapp.service

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * PayPay HMAC-SHA256 認証ヘルパー
 *
 * PayPay OPA API の HMAC 認証を実装します。
 * 参考: https://www.paypay.ne.jp/opa/doc/jp/v1.0/hmac_authentication
 */
class PayPayHmacAuth(
    private val apiKey: String,
    private val apiSecret: String
) {

    /**
     * Authorization ヘッダーを生成
     *
     * @param requestUri リクエストURI（例: "/v2/codes"）
     * @param httpMethod HTTPメソッド（例: "POST"）
     * @param requestBody リクエストボディ（JSON文字列）、GETの場合はnull
     * @param contentType Content-Type、GETの場合は"empty"
     * @return Authorization ヘッダーの値
     */
    fun generateAuthorizationHeader(
        requestUri: String,
        httpMethod: String,
        requestBody: String? = null,
        contentType: String = "application/json;charset=UTF-8;"
    ): String {
        val epoch = (System.currentTimeMillis() / 1000).toString()
        val nonce = generateNonce()

        // ステップ1: MD5ハッシュ生成（GETの場合は"empty"）
        val hash = if (requestBody != null && httpMethod.uppercase() != "GET") {
            generateMd5Hash(contentType, requestBody)
        } else {
            "empty"
        }

        val actualContentType = if (httpMethod.uppercase() == "GET") "empty" else contentType

        // ステップ2: HMAC署名用の文字列を構築
        val stringToSign = buildStringToSign(
            requestUri,
            httpMethod,
            nonce,
            epoch,
            actualContentType,
            hash
        )

        // ステップ3: HMAC-SHA256署名を生成
        val macData = generateHmacSignature(stringToSign)

        // ステップ4: Authorizationヘッダーを構築
        return buildAuthorizationHeader(macData, nonce, epoch, hash)
    }

    /**
     * ステップ1: Content-TypeとリクエストボディのMD5ハッシュを生成
     */
    private fun generateMd5Hash(contentType: String, requestBody: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(contentType.toByteArray(StandardCharsets.UTF_8))
        md.update(requestBody.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(md.digest())
    }

    /**
     * ステップ2: HMAC署名用の文字列を構築
     */
    private fun buildStringToSign(
        requestUri: String,
        httpMethod: String,
        nonce: String,
        epoch: String,
        contentType: String,
        hash: String
    ): String {
        return StringBuilder()
            .append(requestUri)
            .append("\n")
            .append(httpMethod.uppercase())
            .append("\n")
            .append(nonce)
            .append("\n")
            .append(epoch)
            .append("\n")
            .append(contentType)
            .append("\n")
            .append(hash)
            .toString()
    }

    /**
     * ステップ3: HMAC-SHA256署名を生成
     */
    private fun generateHmacSignature(stringToSign: String): String {
        val signingKey = SecretKeySpec(
            apiSecret.toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256"
        )
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(stringToSign.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(rawHmac)
    }

    /**
     * ステップ4: Authorizationヘッダーを構築
     *
     * フォーマット: hmac OPA-Auth:{apiKey}:{macData}:{nonce}:{epoch}:{hash}
     */
    private fun buildAuthorizationHeader(
        macData: String,
        nonce: String,
        epoch: String,
        hash: String
    ): String {
        return "hmac OPA-Auth:$apiKey:$macData:$nonce:$epoch:$hash"
    }

    /**
     * ランダムなNonce文字列を生成（8文字推奨）
     */
    private fun generateNonce(length: Int = 8): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
