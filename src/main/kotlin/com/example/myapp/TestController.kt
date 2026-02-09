package com.example.myapp

// TestController はテスト用エンドポイントで、起動時のリポジトリ/エンティティ関連の問題を引き起こすため無効化しています。
// 必要な場合はこのファイルを復元して使用してください。

/*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.beans.factory.annotation.Autowired

interface TestItemRepository : JpaRepository<TestItem, Long>

@RestController
class TestController {
    @Autowired(required = false)
    private var repository: TestItemRepository? = null

    @GetMapping("/api/test")
    fun getItems(): List<TestItem> {
        return repository?.findAll() ?: emptyList()
    }
}
*/