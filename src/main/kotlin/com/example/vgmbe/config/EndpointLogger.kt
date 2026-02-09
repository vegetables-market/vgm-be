package com.example.vgmbe.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Configuration
class EndpointLogger {
    private val logger = LoggerFactory.getLogger(EndpointLogger::class.java)

    // 複数の RequestMappingHandlerMapping (actuator の controllerEndpointHandlerMapping など) を受け取る
    @Bean
    fun logEndpoints(mappings: Map<String, RequestMappingHandlerMapping>) = ApplicationRunner {
        logger.info("Registered request mappings:")
        mappings.forEach { (name, mapping) ->
            mapping.handlerMethods.forEach { (k, v) ->
                logger.info("{} [{}] => {}", name, k.patternsCondition?.patterns, v.method.toGenericString())
            }
        }
    }
}
