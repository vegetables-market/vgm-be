package com.example.myapp.controller

//import com.example.myapp.dto.*
//import com.example.myapp.entity.PaymentMethod
//import com.example.myapp.entity.User
//import com.example.myapp.repository.OrderRepository
//import com.example.myapp.repository.PaymentRepository
//import com.example.myapp.repository.UserRepository
//import com.example.myapp.service.PayPayPaymentService
//import com.example.myapp.service.StripePaymentService
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//import java.math.BigDecimal
//
//@RestController
//@RequestMapping("/api/payment")
//class PaymentController(
//    private val stripePaymentService: StripePaymentService,
//    private val payPayPaymentService: PayPayPaymentService,
//    private val orderRepository: OrderRepository,
//    private val userRepository: UserRepository,
//    private val paymentRepository: PaymentRepository
//) {
//
//    /**
//     * 決済を開始
//     * Stripe: Payment Intentを作成してclientSecretを返す
//     * PayPay: 決済URLとDeeplinkを返す
//     */
//    // @PostMapping("/create")
//    // fun createPayment(@RequestBody request: CreatePaymentRequest): ResponseEntity<CreatePaymentResponse> {
//    //     try {
//    //         println("Received payment creation request: $request")
//
//    //         val order = orderRepository.findById(request.orderId).orElse(null)
//    //             ?: return ResponseEntity.badRequest().body(
//    //                 CreatePaymentResponse(
//    //                     success = false,
//    //                     message = "注文が見つかりません"
//    //                 )
//    //             )
//
//    //         val user = userRepository.findById(request.userId).orElse(null)
//    //             ?: return ResponseEntity.badRequest().body(
//    //                 CreatePaymentResponse(
//    //                     success = false,
//    //                     message = "ユーザーが見つかりません"
//    //                 )
//    //             )
//
//    //         when (request.paymentMethod) {
//    //             PaymentMethod.CREDIT_CARD -> {
//    //                 val paymentIntent = stripePaymentService.createPaymentIntent(
//    //                     order = order,
//    //                     user = user,
//    //                     amount = request.amount.toBigDecimal()
//    //                 )
//
//    //                 println("Payment intent created successfully: ${paymentIntent.id}")
//
//    //                 return ResponseEntity.ok(
//    //                     CreatePaymentResponse(
//    //                         success = true,
//    //                         message = "Stripe決済を開始しました",
//    //                         clientSecret = paymentIntent.clientSecret
//    //                     )
//    //                 )
//    //             }
//
//    //             PaymentMethod.PAYPAY -> {
//    //                 val paypayRequest = payPayPaymentService.createPaymentRequest(
//    //                     order = order,
//    //                     user = user,
//    //                     amount = request.amount.toBigDecimal()
//    //                 )
//
//    //                 return ResponseEntity.ok(
//    //                     CreatePaymentResponse(
//    //                         success = true,
//    //                         message = "PayPay決済を開始しました",
//    //                         paypayUrl = paypayRequest.url,
//    //                         paypayDeeplink = paypayRequest.deeplink
//    //                     )
//    //                 )
//    //             }
//
//    //             else -> {
//    //                 return ResponseEntity.badRequest().body(
//    //                     CreatePaymentResponse(
//    //                         success = false,
//    //                         message = "サポートされていない決済方法です"
//    //                     )
//    //                 )
//    //             }
//    //         }
//    //     } catch (e: Exception) {
//    //         println("Error creating payment: ${e.message}")
//    //         e.printStackTrace()
//    //         return ResponseEntity.internalServerError().body(
//    //             CreatePaymentResponse(
//    //                 success = false,
//    //                 message = "決済の開始に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//
//    /**
//     * Stripe決済を確認（エスクロー状態にする）
//     */
//    // @PostMapping("/confirm")
//    // fun confirmPayment(@RequestBody request: ConfirmPaymentRequest): ResponseEntity<ConfirmPaymentResponse> {
//    //     try {
//    //         val payment = stripePaymentService.confirmPaymentIntent(request.paymentIntentId)
//
//    //         return ResponseEntity.ok(
//    //             ConfirmPaymentResponse(
//    //                 success = true,
//    //                 message = "決済が完了しました（エスクロー状態）",
//    //                 paymentId = payment.id,
//    //                 status = payment.status
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.badRequest().body(
//    //             ConfirmPaymentResponse(
//    //                 success = false,
//    //                 message = "決済の確認に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//
//    /**
//     * エスクローを解除して出品者に入金
//     * 商品受取確認後に買い手が実行
//     */
//    // @PostMapping("/release-escrow")
//    // fun releaseEscrow(@RequestBody request: ReleaseEscrowRequest): ResponseEntity<ReleaseEscrowResponse> {
//    //     try {
//    //         val transaction = stripePaymentService.releaseEscrow(request.orderId)
//
//    //         return ResponseEntity.ok(
//    //             ReleaseEscrowResponse(
//    //                 success = true,
//    //                 message = "出品者への入金が完了しました",
//    //                 transactionId = transaction.id
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.badRequest().body(
//    //             ReleaseEscrowResponse(
//    //                 success = false,
//    //                 message = "エスクロー解除に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//
//    /**
//     * 返金処理
//     * トラブル時やキャンセル時に実行
//     */
//    // @PostMapping("/refund")
//    // fun refundPayment(@RequestBody request: RefundPaymentRequest): ResponseEntity<RefundPaymentResponse> {
//    //     try {
//    //         val payment = stripePaymentService.refundPayment(request.orderId, request.reason)
//
//    //         return ResponseEntity.ok(
//    //             RefundPaymentResponse(
//    //                 success = true,
//    //                 message = "返金が完了しました",
//    //                 refundId = payment.externalPaymentId
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.badRequest().body(
//    //             RefundPaymentResponse(
//    //                 success = false,
//    //                 message = "返金に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//
//    /**
//     * 決済ステータスを取得
//     */
//    // @GetMapping("/{paymentId}/status")
//    // fun getPaymentStatus(@PathVariable paymentId: Long): ResponseEntity<PaymentStatusResponse> {
//    //     try {
//    //         val payment = paymentRepository.findById(paymentId).orElse(null)
//    //             ?: return ResponseEntity.notFound().build()
//
//    //         return ResponseEntity.ok(
//    //             PaymentStatusResponse(
//    //                 success = true,
//    //                 paymentId = payment.id!!,
//    //                 status = payment.status,
//    //                 amount = payment.amount,
//    //                 paymentMethod = payment.paymentMethod,
//    //                 createdAt = payment.createdAt.toString(),
//    //                 completedAt = payment.completedAt?.toString()
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.internalServerError().build()
//    //     }
//    // }
//
//    /**
//     * Stripe Webhookエンドポイント
//     * 決済イベント（成功、失敗など）を受け取る
//     */
//    @PostMapping("/webhook/stripe")
//    fun handleStripeWebhook(@RequestBody payload: String, @RequestHeader("Stripe-Signature") signature: String): ResponseEntity<String> {
//        // TODO: Stripe Webhookの署名検証と処理
//        // https://stripe.com/docs/webhooks/signatures
//        return ResponseEntity.ok("received")
//    }
//
//    /**
//     * PayPay Webhookエンドポイント
//     * 決済イベントを受け取る
//     */
//    @PostMapping("/webhook/paypay")
//    fun handlePayPayWebhook(@RequestBody payload: String): ResponseEntity<String> {
//        // TODO: PayPay Webhookの処理
//        return ResponseEntity.ok("received")
//    }
//}
