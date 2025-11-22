package com.example.myapp

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController {

    // 詳細ページ (detail.html) を表示する係
    @GetMapping("/items/detail")
    fun detail(): String {
        return "pages/detail"
    }
}