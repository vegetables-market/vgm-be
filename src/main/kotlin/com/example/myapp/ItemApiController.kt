package com.example.myapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
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

    @GetMapping("/api/items")
    fun getItems(): List<Item> {
        // 1. JSONファイルを読み込む準備
        // (src/main/resources/data/items.json を探す)
        val resource = ClassPathResource("data/items.json")

        // 2. JSON変換機 (Jackson) を用意
        val mapper = ObjectMapper().registerKotlinModule()

        // 3. ファイルの中身を読んで、List<Item> に自動変換して返す
        val items: List<Item> = mapper.readValue(resource.inputStream)

        return items
    }
}