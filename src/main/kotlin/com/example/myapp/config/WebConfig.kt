package com.example.myapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${cors.allowed-origins}") private val allowedOrigins: String
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        val origins = allowedOrigins.split(",").map { it.trim() }.toTypedArray()
        
        registry.addMapping("/**")
            .allowedOrigins(*origins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
