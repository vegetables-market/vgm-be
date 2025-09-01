package com.example.myapp.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProfileController {

    @GetMapping("/profile")
    fun profile(): String {
        return "user/profile"
    }
}