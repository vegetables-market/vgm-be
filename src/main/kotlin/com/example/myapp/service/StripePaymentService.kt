package com.example.myapp.service

import com.example.myapp.entity.*
import com.example.myapp.repository.PaymentRepository
import com.example.myapp.repository.TransactionRepository
import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class StripePaymentService(
    private val paymentRepository: PaymentRepository,
    private val transactionRepository: TransactionRepository,
    @Value("\${stripe.api.key}") private val stripeApiKey: String
) {

    init {
        Stripe.apiKey = stripeApiKey
    }

    /**
     * Payment Intentを作成（クレジットカード決済の開始）
     * エスクロー用に資金を保留する
     */
    fun createPaymentIntent(
        order: Order,
        user: User,
        amount: BigDecimal
    ): PaymentIntent {
        val params = PaymentIntentCreateParams.builder()
            .setAmount((amount.multiply(BigDecimal(100))).toLong()) // 金額をセント単位に変換
            .setCurrency("jpy")
            .addPaymentMethodType("card") // クレジットカードのみを許可（リダイレクト不要）
            .putMetadata("orderId", order.id.toString())
            .putMetadata("userId", user.id.toString())
            .putMetadata("sellerAmount", order.sellerAmount.toString())
            .putMetadata("platformFee", order.platformFee.toString())
            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL) // エスクロー用に手動キャプチャ
            .build()

        val paymentIntent = PaymentIntent.create(params)

        // Paymentレコード作成
        val payment = Payment(
            order = order,
            user = user,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = amount,
            status = PaymentStatus.PENDING,
            externalPaymentId = paymentIntent.id,
            createdAt = LocalDateTime.now()
        )
        paymentRepository.save(payment)

        return paymentIntent
    }

    /**
     * Payment Intentを確認（支払い実行）
     * エスクロー状態にして資金を保留
     * 注: フロントエンドで既にconfirmCardPayment()を実行しているため、
     * ここではPayment Intentの状態確認とトランザクション作成のみを行う
     */
    fun confirmPaymentIntent(paymentIntentId: String): Payment {
        val paymentIntent = PaymentIntent.retrieve(paymentIntentId)

        // Payment Intentの状態を確認
        // succeeded: 即座に確定された決済
        // requires_capture: 手動キャプチャモード（エスクロー）で認証完了
        if (paymentIntent.status != "succeeded" && paymentIntent.status != "requires_capture") {
            throw RuntimeException("Payment Intent is not in a valid state: ${paymentIntent.status}")
        }

        val payment = paymentRepository.findByExternalPaymentId(paymentIntentId)
            .orElseThrow { RuntimeException("Payment not found") }

        // ステータスを更新
        val updatedPayment = payment.copy(
            status = PaymentStatus.COMPLETED,
            completedAt = LocalDateTime.now()
        )
        paymentRepository.save(updatedPayment)

        // Transactionレコードを作成（エスクロー状態）
        val transaction = Transaction(
            order = payment.order,
            type = TransactionType.PAYMENT,
            amount = payment.amount,
            status = TransactionStatus.ESCROWED,
            escrowStartedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )
        transactionRepository.save(transaction)

        return updatedPayment
    }

    /**
     * エスクローを解除して出品者に入金
     * 商品受取確認後に実行
     */
    fun releaseEscrow(orderId: Long): Transaction {
        // 注文IDでトランザクションを検索
        val transaction = transactionRepository.findAll()
            .firstOrNull { it.order.id == orderId }
            ?: throw RuntimeException("Transaction not found")

        if (transaction.status != TransactionStatus.ESCROWED) {
            throw RuntimeException("Transaction is not in escrowed status")
        }

        val payment = paymentRepository.findByOrder(transaction.order).firstOrNull()
            ?: throw RuntimeException("Payment not found")

        // Payment Intentをキャプチャして決済確定
        val paymentIntent = PaymentIntent.retrieve(payment.externalPaymentId)
        paymentIntent.capture()

        // Transactionを完了状態に更新
        val updatedTransaction = transaction.copy(
            status = TransactionStatus.COMPLETED,
            escrowReleasedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        transactionRepository.save(updatedTransaction)

        // TODO: 実際の入金処理（Stripe Connectを使用して出品者アカウントに送金）
        // この実装にはStripe Connectアカウントのセットアップが必要

        return updatedTransaction
    }

    /**
     * 返金処理
     * トラブル時やキャンセル時に実行
     */
    fun refundPayment(orderId: Long, reason: String? = null): Payment {
        // 注文IDで決済を検索
        val payment = paymentRepository.findAll()
            .firstOrNull { it.order.id == orderId }
            ?: throw RuntimeException("Payment not found")

        if (payment.status != PaymentStatus.COMPLETED) {
            throw RuntimeException("Payment cannot be refunded")
        }

        // Stripeで返金処理
        val refundParams = RefundCreateParams.builder()
            .setPaymentIntent(payment.externalPaymentId)
            .apply { reason?.let { setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER) } }
            .build()

        val refund = Refund.create(refundParams)

        // Paymentステータスを更新
        val updatedPayment = payment.copy(
            status = PaymentStatus.REFUNDED,
            metadata = """{"refundId": "${refund.id}", "refundReason": "$reason"}"""
        )
        paymentRepository.save(updatedPayment)

        // Transactionを更新
        val transaction = transactionRepository.findByOrder(payment.order)
            .orElseThrow { RuntimeException("Transaction not found") }

        val updatedTransaction = transaction.copy(
            status = TransactionStatus.REFUNDED,
            refundedAt = LocalDateTime.now(),
            notes = reason
        )
        transactionRepository.save(updatedTransaction)

        return updatedPayment
    }

    /**
     * Payment Intentのステータスを取得
     */
    fun getPaymentIntentStatus(paymentIntentId: String): String {
        val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
        return paymentIntent.status
    }
}
