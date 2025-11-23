package com.example.myapp.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "OK",
            "message" to "VGM Backend is running"
        )
    }

//    @GetMapping("/")
//    fun root(): Map<String, String> {
//        return mapOf(
//            "service" to "VGM Backend API",
//            "version" to "0.0.1-SNAPSHOT"
//        )
//    }
}