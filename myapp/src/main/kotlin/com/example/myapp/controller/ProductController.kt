package com.example.myapp.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProductController {

    @GetMapping("/product")
    fun product(): String {
        return "product/productPage"
    }
}