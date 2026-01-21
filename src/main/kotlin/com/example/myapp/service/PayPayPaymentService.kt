package com.example.myapp.service
//
//import com.example.myapp.entity.*
//import com.example.myapp.repository.PaymentRepository
//import com.example.myapp.repository.TransactionRepository
//import com.fasterxml.jackson.databind.JsonNode
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.http.HttpHeaders
//import org.springframework.http.MediaType
//import org.springframework.stereotype.Service
//import org.springframework.web.reactive.function.client.WebClient
//import org.springframework.web.reactive.function.client.bodyToMono
//import java.math.BigDecimal
//import java.time.LocalDateTime
//import java.util.*
//
///**
// * PayPay決済サービス
// *
// * PayPay OPA (Open Payment API) を使用した実際の決済処理を実装
// * 参考: https://www.paypay.ne.jp/opa/doc/v1.0/dynamicqrcode
// */
//@Service
//class PayPayPaymentService(
//    private val paymentRepository: PaymentRepository,
//    private val transactionRepository: TransactionRepository,
//    @Value("\${paypay.api.key:}") private val paypayApiKey: String,
//    @Value("\${paypay.api.secret:}") private val paypayApiSecret: String,
//    @Value("\${paypay.merchant.id:}") private val paypayMerchantId: String,
//    @Value("\${paypay.api.baseUrl:https://stg-api.paypay.ne.jp}") private val baseUrl: String
//) {
//
//    private val logger = LoggerFactory.getLogger(PayPayPaymentService::class.java)
//    private val webClient = WebClient.builder().baseUrl(baseUrl).build()
//    private val objectMapper = ObjectMapper()
//    private val hmacAuth = PayPayHmacAuth(paypayApiKey, paypayApiSecret)
//
//    /**
//     * PayPay QRコード決済リクエストを作成
//     * POST /v2/codes
//     */
//    fun createPaymentRequest(
//        order: Order,
//        user: User,
//        amount: BigDecimal
//    ): PayPayPaymentRequest {
//        val merchantPaymentId = "ORDER_${order.id}_${UUID.randomUUID().toString().substring(0, 8)}"
//
//        // Paymentレコード作成
//        val payment = Payment(
//            order = order,
//            user = user,
//            paymentMethod = PaymentMethod.PAYPAY,
//            amount = amount,
//            status = PaymentStatus.PENDING,
//            externalPaymentId = merchantPaymentId,
//            createdAt = LocalDateTime.now()
//        )
//        paymentRepository.save(payment)
//
//        // PayPay API リクエストボディ
//        val requestBody = mapOf(
//            "merchantPaymentId" to merchantPaymentId,
//            "codeType" to "ORDER_QR",
//            "amount" to mapOf(
//                "amount" to amount.toInt(),
//                "currency" to "JPY"
//            ),
//            "orderDescription" to "注文 #${order.id}",
//            "isAuthorization" to false,
//            "redirectUrl" to "https://example.com/payment/callback",
//            "redirectType" to "WEB_LINK"
//        )
//
//        val requestBodyJson = objectMapper.writeValueAsString(requestBody)
//        val requestUri = "/v2/codes"
//
//        // HMAC認証ヘッダー生成
//        val authHeader = hmacAuth.generateAuthorizationHeader(
//            requestUri = requestUri,
//            httpMethod = "POST",
//            requestBody = requestBodyJson
//        )
//
//        logger.info("PayPay QRコード作成リクエスト: merchantPaymentId=$merchantPaymentId")
//
//        try {
//            // PayPay API 呼び出し
//            val response = webClient.post()
//                .uri(requestUri)
//                .header(HttpHeaders.AUTHORIZATION, authHeader)
//                .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8;")
//                .bodyValue(requestBodyJson)
//                .retrieve()
//                .bodyToMono<String>()
//                .block()
//
//            val jsonResponse = objectMapper.readTree(response)
//            val data = jsonResponse.get("data")
//
//            return PayPayPaymentRequest(
//                merchantPaymentId = merchantPaymentId,
//                amount = amount,
//                codeId = data.get("codeId").asText(),
//                url = data.get("url").asText(),
//                deeplink = data.get("deeplink").asText()
//            )
//
//        } catch (e: Exception) {
//            logger.error("PayPay API エラー: ${e.message}", e)
//            throw RuntimeException("PayPay QRコード作成に失敗しました: ${e.message}")
//        }
//    }
//
//    /**
//     * PayPay決済のステータスを確認
//     * GET /v2/codes/payments/{merchantPaymentId}
//     */
//    fun checkPaymentStatus(merchantPaymentId: String): PaymentStatus {
//        val payment = paymentRepository.findByExternalPaymentId(merchantPaymentId)
//            .orElseThrow { RuntimeException("Payment not found") }
//
//        val requestUri = "/v2/codes/payments/$merchantPaymentId"
//
//        // HMAC認証ヘッダー生成（GETリクエスト）
//        val authHeader = hmacAuth.generateAuthorizationHeader(
//            requestUri = requestUri,
//            httpMethod = "GET"
//        )
//
//        logger.info("PayPay 決済状態確認: merchantPaymentId=$merchantPaymentId")
//
//        try {
//            val response = webClient.get()
//                .uri(requestUri)
//                .header(HttpHeaders.AUTHORIZATION, authHeader)
//                .retrieve()
//                .bodyToMono<String>()
//                .block()
//
//            val jsonResponse = objectMapper.readTree(response)
//            val data = jsonResponse.get("data")
//            val status = data.get("status").asText()
//
//            // PayPayのステータスをマッピング
//            return when (status) {
//                "COMPLETED" -> PaymentStatus.COMPLETED
//                "AUTHORIZED" -> PaymentStatus.PROCESSING
//                "FAILED" -> PaymentStatus.FAILED
//                else -> PaymentStatus.PENDING
//            }
//
//        } catch (e: Exception) {
//            logger.error("PayPay API エラー: ${e.message}", e)
//            return payment.status
//        }
//    }
//
//    /**
//     * PayPay決済を確定（エスクロー状態）
//     * Webhookまたはポーリングから呼ばれることを想定
//     */
//    fun confirmPayment(merchantPaymentId: String, paypayPaymentId: String): Payment {
//        val payment = paymentRepository.findByExternalPaymentId(merchantPaymentId)
//            .orElseThrow { RuntimeException("Payment not found") }
//
//        // ステータスを更新
//        val updatedPayment = payment.copy(
//            status = PaymentStatus.COMPLETED,
//            externalChargeId = paypayPaymentId,
//            completedAt = LocalDateTime.now(),
//            metadata = """{"paypayPaymentId": "$paypayPaymentId"}"""
//        )
//        paymentRepository.save(updatedPayment)
//
//        // Transactionレコードを作成（エスクロー状態）
//        val transaction = Transaction(
//            order = payment.order,
//            type = TransactionType.PAYMENT,
//            amount = payment.amount,
//            status = TransactionStatus.ESCROWED,
//            escrowStartedAt = LocalDateTime.now(),
//            createdAt = LocalDateTime.now()
//        )
//        transactionRepository.save(transaction)
//
//        logger.info("PayPay決済確定: merchantPaymentId=$merchantPaymentId, paypayPaymentId=$paypayPaymentId")
//
//        return updatedPayment
//    }
//
//    /**
//     * PayPay決済をキャンセル
//     * DELETE /v2/payments/{merchantPaymentId}
//     */
//    fun cancelPayment(merchantPaymentId: String): Payment {
//        val payment = paymentRepository.findByExternalPaymentId(merchantPaymentId)
//            .orElseThrow { RuntimeException("Payment not found") }
//
//        val requestUri = "/v2/payments/$merchantPaymentId"
//
//        // HMAC認証ヘッダー生成（DELETEリクエスト）
//        val authHeader = hmacAuth.generateAuthorizationHeader(
//            requestUri = requestUri,
//            httpMethod = "DELETE"
//        )
//
//        logger.info("PayPay決済キャンセル: merchantPaymentId=$merchantPaymentId")
//
//        try {
//            webClient.delete()
//                .uri(requestUri)
//                .header(HttpHeaders.AUTHORIZATION, authHeader)
//                .retrieve()
//                .bodyToMono<String>()
//                .block()
//
//            val updatedPayment = payment.copy(
//                status = PaymentStatus.FAILED,
//                metadata = """{"cancelled": true}"""
//            )
//            paymentRepository.save(updatedPayment)
//
//            return updatedPayment
//
//        } catch (e: Exception) {
//            logger.error("PayPay API エラー: ${e.message}", e)
//            throw RuntimeException("PayPay決済キャンセルに失敗しました: ${e.message}")
//        }
//    }
//
//    /**
//     * 返金処理
//     * POST /v2/refunds
//     */
//    fun refundPayment(merchantPaymentId: String, reason: String? = null): Payment {
//        val payment = paymentRepository.findByExternalPaymentId(merchantPaymentId)
//            .orElseThrow { RuntimeException("Payment not found") }
//
//        if (payment.status != PaymentStatus.COMPLETED) {
//            throw RuntimeException("Payment cannot be refunded")
//        }
//
//        val merchantRefundId = "REFUND_${payment.id}_${UUID.randomUUID().toString().substring(0, 8)}"
//        val requestedAt = System.currentTimeMillis() / 1000
//
//        // PayPay API リクエストボディ
//        val requestBody = mapOf(
//            "merchantRefundId" to merchantRefundId,
//            "paymentId" to (payment.externalChargeId ?: merchantPaymentId),
//            "amount" to mapOf(
//                "amount" to payment.amount.toInt(),
//                "currency" to "JPY"
//            ),
//            "requestedAt" to requestedAt,
//            "reason" to (reason ?: "顧客による返品")
//        )
//
//        val requestBodyJson = objectMapper.writeValueAsString(requestBody)
//        val requestUri = "/v2/refunds"
//
//        // HMAC認証ヘッダー生成
//        val authHeader = hmacAuth.generateAuthorizationHeader(
//            requestUri = requestUri,
//            httpMethod = "POST",
//            requestBody = requestBodyJson
//        )
//
//        logger.info("PayPay返金リクエスト: merchantRefundId=$merchantRefundId")
//
//        try {
//            webClient.post()
//                .uri(requestUri)
//                .header(HttpHeaders.AUTHORIZATION, authHeader)
//                .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8;")
//                .bodyValue(requestBodyJson)
//                .retrieve()
//                .bodyToMono<String>()
//                .block()
//
//            // Paymentステータスを更新
//            val updatedPayment = payment.copy(
//                status = PaymentStatus.REFUNDED,
//                metadata = """{"refundReason": "$reason", "merchantRefundId": "$merchantRefundId"}"""
//            )
//            paymentRepository.save(updatedPayment)
//
//            // Transactionを更新
//            val transaction = transactionRepository.findByOrder(payment.order)
//                .orElseThrow { RuntimeException("Transaction not found") }
//
//            val updatedTransaction = transaction.copy(
//                status = TransactionStatus.REFUNDED,
//                refundedAt = LocalDateTime.now(),
//                notes = reason
//            )
//            transactionRepository.save(updatedTransaction)
//
//            logger.info("PayPay返金完了: merchantRefundId=$merchantRefundId")
//
//            return updatedPayment
//
//        } catch (e: Exception) {
//            logger.error("PayPay API エラー: ${e.message}", e)
//            throw RuntimeException("PayPay返金処理に失敗しました: ${e.message}")
//        }
//    }
//}
//
//data class PayPayPaymentRequest(
//    val merchantPaymentId: String,
//    val amount: BigDecimal,
//    val codeId: String,
//    val url: String,
//    val deeplink: String
//)
