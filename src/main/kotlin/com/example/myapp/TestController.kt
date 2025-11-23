package com.example.myapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.jpa.repository.JpaRepository

interface TestItemRepository : JpaRepository<TestItem, Long>

@RestController
class TestController(val repository: TestItemRepository) {
    @GetMapping("/api/test")
    fun getItems(): List<TestItem> {
        return repository.findAll()
    }
}