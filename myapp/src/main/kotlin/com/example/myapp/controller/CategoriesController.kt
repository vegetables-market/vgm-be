package com.example.myapp.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CategoriesController {

    @GetMapping("/categories")
    fun categories(): String {
        return "category/categories"
    }
}