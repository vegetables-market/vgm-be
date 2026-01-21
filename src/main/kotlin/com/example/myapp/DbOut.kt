package com.example.myapp

import jakarta.persistence.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@Entity
@Table(name = "t_items")
data class Item(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    @Column(name = "f_arrival_date")
    val arrivalDate: LocalDateTime? = null
    // 必要に応じて他のフィールドを追加
)

interface ItemRepository : JpaRepository<Item, Long>

@RestController
class ItemController(val repository: ItemRepository) {
    @GetMapping("/api/items")
    fun getItems(@RequestParam(defaultValue = "asc") sort: String): List<Item> {
        val direction = if (sort == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return repository.findAll(Sort.by(direction, "arrival_date"))
    }
}