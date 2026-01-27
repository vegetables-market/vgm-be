package com.example.myapp.controller

//import com.example.myapp.dto.*
//import com.example.myapp.entity.market.Order
//import com.example.myapp.entity.market.OrderStatus
//import com.example.myapp.entity.market.ProductStatus
//import com.example.myapp.repository.market.OrderRepository
//import com.example.myapp.repository.market.ProductRepository
//import com.example.myapp.repository.UserRepository
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//import java.math.BigDecimal
//
//@RestController
//@RequestMapping("/api/orders")
//class OrderController(
//    private val orderRepository: OrderRepository,
//    private val productRepository: ProductRepository,
//    private val userRepository: UserRepository,
//    @Value("\${marketplace.platform-fee-rate:0.10}") private val platformFeeRate: Double
//) {
//
//    // @PostMapping
//    // fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<CreateOrderResponse> {
//    //     try {
//    //         println("Received order creation request: $request")
//
//    //         val buyer = userRepository.findById(request.buyerId).orElse(null)
//    //             ?: return ResponseEntity.badRequest().body(
//    //                 CreateOrderResponse(
//    //                     success = false,
//    //                     message = "買い手が見つかりません"
//    //                 )
//    //             )
//
//    //         val product = productRepository.findById(request.productId).orElse(null)
//    //             ?: return ResponseEntity.badRequest().body(
//    //                 CreateOrderResponse(
//    //                     success = false,
//    //                     message = "商品が見つかりません"
//    //                 )
//    //             )
//
//    //         if (product.status != ProductStatus.AVAILABLE) {
//    //             return ResponseEntity.badRequest().body(
//    //                 CreateOrderResponse(
//    //                     success = false,
//    //                     message = "この商品は購入できません"
//    //                 )
//    //             )
//    //         }
//
//    //         if (product.seller.id == buyer.id) {
//    //             return ResponseEntity.badRequest().body(
//    //                 CreateOrderResponse(
//    //                     success = false,
//    //                     message = "自分の商品は購入できません"
//    //                 )
//    //             )
//    //         }
//
//    //         // 金額計算
//    //         val totalAmount = product.price
//    //         val platformFee = totalAmount.multiply(BigDecimal(platformFeeRate))
//    //         val sellerAmount = totalAmount.subtract(platformFee)
//
//    //         val order = Order(
//    //             buyer = buyer,
//    //             seller = product.seller,
//    //             product = product,
//    //             totalAmount = totalAmount,
//    //             platformFee = platformFee,
//    //             sellerAmount = sellerAmount,
//    //             status = OrderStatus.PENDING_PAYMENT,
//    //             shippingAddress = request.shippingAddress,
//    //             shippingPostalCode = request.shippingPostalCode,
//    //             shippingRecipientName = request.shippingRecipientName,
//    //             shippingPhoneNumber = request.shippingPhoneNumber
//    //         )
//
//    //         val savedOrder = orderRepository.save(order)
//
//    //         println("Order created successfully: ${savedOrder.id}")
//
//    //         // 商品を予約済みに変更
//    //         val updatedProduct = product.copy(status = ProductStatus.RESERVED)
//    //         productRepository.save(updatedProduct)
//
//    //         return ResponseEntity.ok(
//    //             CreateOrderResponse(
//    //                 success = true,
//    //                 message = "注文を作成しました",
//    //                 orderId = savedOrder.id,
//    //                 totalAmount = savedOrder.totalAmount.toDouble(),
//    //                 platformFee = savedOrder.platformFee.toDouble(),
//    //                 sellerAmount = savedOrder.sellerAmount.toDouble()
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         println("Error creating order: ${e.message}")
//    //         e.printStackTrace()
//    //         return ResponseEntity.internalServerError().body(
//    //             CreateOrderResponse(
//    //                 success = false,
//    //                 message = "注文の作成に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//
//    // @GetMapping("/{orderId}")
//    // fun getOrder(@PathVariable orderId: Long): ResponseEntity<OrderDetailResponse> {
//    //     try {
//    //         val order = orderRepository.findById(orderId).orElse(null)
//    //             ?: return ResponseEntity.notFound().build()
//
//    //         return ResponseEntity.ok(
//    //             OrderDetailResponse(
//    //                 success = true,
//    //                 orderId = order.id!!,
//    //                 productName = order.product.name,
//    //                 productPrice = order.product.price,
//    //                 buyerName = order.buyer.username,
//    //                 sellerName = order.seller.username,
//    //                 totalAmount = order.totalAmount,
//    //                 platformFee = order.platformFee,
//    //                 sellerAmount = order.sellerAmount,
//    //                 status = order.status,
//    //                 createdAt = order.createdAt.toString()
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.internalServerError().build()
//    //     }
//    // }
//
//    // @PutMapping("/status")
//    // fun updateOrderStatus(@RequestBody request: UpdateOrderStatusRequest): ResponseEntity<UpdateOrderStatusResponse> {
//    //     try {
//    //         val order = orderRepository.findById(request.orderId).orElse(null)
//    //             ?: return ResponseEntity.badRequest().body(
//    //                 UpdateOrderStatusResponse(
//    //                     success = false,
//    //                     message = "注文が見つかりません"
//    //                 )
//    //             )
//
//    //         val updatedOrder = order.copy(status = request.status)
//    //         orderRepository.save(updatedOrder)
//
//    //         return ResponseEntity.ok(
//    //             UpdateOrderStatusResponse(
//    //                 success = true,
//    //                 message = "注文ステータスを更新しました"
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.badRequest().body(
//    //             UpdateOrderStatusResponse(
//    //                 success = false,
//    //                 message = "ステータス更新に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//}
