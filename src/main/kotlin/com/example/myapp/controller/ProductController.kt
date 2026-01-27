package com.example.myapp.controller

//import com.example.myapp.dto.*
//import com.example.myapp.entity.market.Product
//import com.example.myapp.entity.market.ProductStatus
//import com.example.myapp.repository.market.ProductRepository
//import com.example.myapp.repository.UserRepository
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/api/products")
//class ProductController(
//    private val productRepository: ProductRepository,
//    private val userRepository: UserRepository
//) {
//
//    // @PostMapping
//    // fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<CreateProductResponse> {
//    //     try {
//    //         println("Received product creation request: $request")
//
//    //         val seller = userRepository.findById(request.sellerId).orElse(null)
//    //             ?: return ResponseEntity.badRequest().body(
//    //                 CreateProductResponse(
//    //                     success = false,
//    //                     message = "出品者が見つかりません（sellerId: ${request.sellerId}）"
//    //                 )
//    //             )
//
//    //         val product = Product(
//    //             name = request.name,
//    //             description = request.description,
//    //             price = request.price.toBigDecimal(),
//    //             seller = seller,
//    //             status = ProductStatus.AVAILABLE,
//    //             category = request.category,
//    //             stock = request.stock ?: 1
//    //         )
//
//    //         val savedProduct = productRepository.save(product)
//    //         println("Product created successfully: ${savedProduct.id}")
//
//    //         return ResponseEntity.ok(
//    //             CreateProductResponse(
//    //                 success = true,
//    //                 message = "商品を出品しました",
//    //                 productId = savedProduct.id
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         println("Error creating product: ${e.message}")
//    //         e.printStackTrace()
//    //         return ResponseEntity.badRequest().body(
//    //             CreateProductResponse(
//    //                 success = false,
//    //                 message = "商品の出品に失敗しました: ${e.message}"
//    //             )
//    //         )
//    //     }
//    // }
//
//    // @GetMapping
//    // fun getProducts(): ResponseEntity<ProductListResponse> {
//    //     try {
//    //         println("Getting products...")
//    //         val products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.AVAILABLE)
//    //         println("Found ${products.size} products")
//
//    //         val productDtos = products.map { product ->
//    //             println("Mapping product: ${product.id} - ${product.name}")
//    //             ProductDto(
//    //                 id = product.id!!,
//    //                 name = product.name,
//    //                 description = product.description,
//    //                 price = product.price,
//    //                 sellerId = product.seller.id!!,
//    //                 sellerName = product.seller.username,
//    //                 status = product.status,
//    //                 category = product.category,
//    //                 createdAt = product.createdAt.toString()
//    //             )
//    //         }
//
//    //         println("Successfully mapped ${productDtos.size} products")
//    //         return ResponseEntity.ok(
//    //             ProductListResponse(
//    //                 success = true,
//    //                 products = productDtos
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         println("Error getting products: ${e.message}")
//    //         e.printStackTrace()
//    //         return ResponseEntity.internalServerError().body(
//    //             ProductListResponse(
//    //                 success = false,
//    //                 products = emptyList()
//    //             )
//    //         )
//    //     }
//    // }
//
//    // @GetMapping("/{productId}")
//    // fun getProduct(@PathVariable productId: Long): ResponseEntity<ProductDto> {
//    //     try {
//    //         val product = productRepository.findById(productId).orElse(null)
//    //             ?: return ResponseEntity.notFound().build()
//
//    //         return ResponseEntity.ok(
//    //             ProductDto(
//    //                 id = product.id!!,
//    //                 name = product.name,
//    //                 description = product.description,
//    //                 price = product.price,
//    //                 sellerId = product.seller.id!!,
//    //                 sellerName = product.seller.username,
//    //                 status = product.status,
//    //                 category = product.category,
//    //                 createdAt = product.createdAt.toString()
//    //             )
//    //         )
//    //     } catch (e: Exception) {
//    //         return ResponseEntity.internalServerError().build()
//    //     }
//    // }
//}
