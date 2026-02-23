package com.example.myapp.controller.market.order

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/orders")
class OrderController {

    @PostMapping
    fun createOrder(@RequestBody request: Map<String, Any>): ResponseEntity<Any> {
        println("注文リクエストを受信: $request")
        
        return ResponseEntity.ok(mapOf(
            "orderId" to 123,
            "totalAmount" to 1454,
            "status" to 1
        ))
    }
    @PostMapping("/{orderId}/pay")
    fun payOrder(@PathVariable orderId: String, @RequestBody request: Map<String, Any>): ResponseEntity<Any> {
        println("注文ID: $orderId の決済処理（$request）を受信")
        return ResponseEntity.ok(mapOf("status" to "success"))
    }
}