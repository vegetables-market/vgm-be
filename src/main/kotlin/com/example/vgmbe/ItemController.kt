package com.example.vgmbe

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/items") // ここが URL の「/api/items」に対応します
class ItemController(private val repository: ItemRepository) {

    @PostMapping
    fun create(@RequestBody request: ItemRequest): Item {
        // 受け取ったデータを Item オブジェクトに変換して保存
        return repository.save(Item(name = request.name, price = request.price))
    }
    
    @GetMapping
    fun getAll(): List<Item> {
        return repository.findAll()
    }
}

// リクエストを受け取るためのデータ形式
data class ItemRequest(val name: String, val price: Int)