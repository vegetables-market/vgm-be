package com.example.vgmbe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication(scanBasePackages = ["com.example"])
@EnableJpaRepositories(basePackages = ["com.example"])
@EntityScan(basePackages = ["com.example"])
class Main

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}