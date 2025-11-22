package com.example.myapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

// データクラス (JSONの形と合わせる)
data class Item(
    val id: Int,
    val name: String,
    val price: Int,
    val producer: String,
    val description: String,
    val imageUrl: String
)

@RestController
class ItemApiController {

    // 全リストを返す (一覧ページ用)
    // URL: /api/items
    @GetMapping("/api/items")
    fun getItems(): List<Item> {
        // JSONファイルを読み込んでリストにして返す
        val resource = ClassPathResource("data/items.json")
        val mapper = ObjectMapper().registerKotlinModule()
        val items: List<Item> = mapper.readValue(resource.inputStream)
        
        return items
    }

    // ID指定で1個だけ返す (詳細ページ用)
    // URL: /api/items/1 とか /api/items/3
    @GetMapping("/api/items/{id}")
    fun getItem(@PathVariable id: Int): Item? {
        // 1. JSONを全部読み込む
        val resource = ClassPathResource("data/items.json")
        val mapper = ObjectMapper().registerKotlinModule()
        val items: List<Item> = mapper.readValue(resource.inputStream)

        // 2. 指定されたID (it.id) と一致する商品を探して返す
        // 見つからなかったら null が返る
        return items.find { it.id == id }
    }
}