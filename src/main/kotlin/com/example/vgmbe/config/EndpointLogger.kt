package com.example.vgmbe.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Configuration
class EndpointLogger {
    private val logger = LoggerFactory.getLogger(EndpointLogger::class.java)

    @Bean
    fun logEndpoints(mapping: RequestMappingHandlerMapping) = ApplicationRunner {
        val mappings = mapping.handlerMethods
        logger.info("Registered request mappings:")
        mappings.forEach { (k, v) ->
            logger.info("{} => {}", k.patternsCondition?.patterns, v.method.toGenericString())
        }
    }
}
