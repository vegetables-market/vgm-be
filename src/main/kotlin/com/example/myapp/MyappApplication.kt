package com.example.myapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MyappApplication

fun main(args: Array<String>) {
	runApplication<MyappApplication>(*args)
}
