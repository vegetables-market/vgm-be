package com.example.myapp.service.market.checkout

import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.stripe.exception.StripeException
import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.net.RequestOptions
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class StripeCheckoutPaymentService(
    @Value("\${stripe.api.key:}") private val stripeApiKey: String,
) {
    fun createPaymentIntent(
        orderId: Long,
        userId: Int,
        amount: Long,
        idempotencyKey: String?,
    ): StripePaymentIntent {
        ensureStripeApiKey()
        Stripe.apiKey = stripeApiKey

        try {
            val params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("jpy")
                .addPaymentMethodType("card")
                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                .putMetadata("orderId", orderId.toString())
                .putMetadata("userId", userId.toString())
                .build()

            val requestOptions = RequestOptions.builder()
                .setIdempotencyKey(buildIdempotencyKey(orderId, idempotencyKey))
                .build()

            val paymentIntent = PaymentIntent.create(params, requestOptions)
            val clientSecret = paymentIntent.clientSecret
                ?: throw AppException(ErrorCode.PAYMENT_FAILED, "Stripe client secret is missing")

            return StripePaymentIntent(
                paymentIntentId = paymentIntent.id,
                clientSecret = clientSecret,
                status = paymentIntent.status ?: "requires_payment_method",
            )
        } catch (e: AppException) {
            throw e
        } catch (e: StripeException) {
            throw AppException(
                errorCode = ErrorCode.PAYMENT_FAILED,
                details = listOfNotNull(
                    "Stripe create failed",
                    e.code?.let { "code=$it" },
                    e.statusCode?.let { "http_status=$it" },
                    e.message?.let { "message=$it" },
                ),
            )
        } catch (e: Exception) {
            throw AppException(
                errorCode = ErrorCode.PAYMENT_FAILED,
                details = listOfNotNull(
                    "Stripe create failed",
                    e.message?.let { "message=$it" },
                ),
            )
        }
    }

    fun verifyPaymentIntent(
        paymentIntentId: String,
        orderId: Long,
        expectedAmount: Long,
    ): VerifiedStripePayment {
        ensureStripeApiKey()
        Stripe.apiKey = stripeApiKey

        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            val status = paymentIntent.status ?: "unknown"
            if (status != "requires_capture" && status != "succeeded") {
                throw AppException(ErrorCode.PAYMENT_FAILED, "Payment is not authorized (status=$status)")
            }

            val metadataOrderId = paymentIntent.metadata["orderId"]?.toLongOrNull()
            if (metadataOrderId != orderId) {
                throw AppException(ErrorCode.PAYMENT_FAILED, "Payment intent does not match order")
            }

            if (paymentIntent.amount != expectedAmount) {
                throw AppException(ErrorCode.PAYMENT_FAILED, "Payment amount does not match order total")
            }

            return VerifiedStripePayment(
                paymentIntentId = paymentIntent.id,
                status = status,
            )
        } catch (e: AppException) {
            throw e
        } catch (e: StripeException) {
            throw AppException(
                errorCode = ErrorCode.PAYMENT_FAILED,
                details = listOfNotNull(
                    "Stripe verify failed",
                    e.code?.let { "code=$it" },
                    e.statusCode?.let { "http_status=$it" },
                    e.message?.let { "message=$it" },
                ),
            )
        } catch (e: Exception) {
            throw AppException(
                errorCode = ErrorCode.PAYMENT_FAILED,
                details = listOfNotNull(
                    "Stripe verify failed",
                    e.message?.let { "message=$it" },
                ),
            )
        }
    }

    private fun ensureStripeApiKey() {
        if (stripeApiKey.isBlank()) {
            throw AppException(ErrorCode.SYSTEM_ERROR, "Stripe API key is not configured")
        }
        if (!stripeApiKey.startsWith("sk_")) {
            throw AppException(
                errorCode = ErrorCode.SYSTEM_ERROR,
                details = listOf("STRIPE_API_KEY must start with sk_"),
            )
        }
    }

    private fun buildIdempotencyKey(orderId: Long, rawIdempotencyKey: String?): String {
        val sanitized = rawIdempotencyKey?.trim()?.takeIf { it.isNotEmpty() } ?: "default"
        return "checkout-$orderId-$sanitized"
    }
}

data class StripePaymentIntent(
    val paymentIntentId: String,
    val clientSecret: String,
    val status: String,
)

data class VerifiedStripePayment(
    val paymentIntentId: String,
    val status: String,
)
