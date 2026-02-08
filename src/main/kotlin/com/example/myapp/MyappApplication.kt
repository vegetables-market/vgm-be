package com.example.myapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * アプリケーションのエントリーポイント
 *
 * - @SpringBootApplication: Spring Bootの自動設定、コンポーネントスキャン、設定クラスの定義を有効化します。
 * - @EnableScheduling: 定期実行処理（@Scheduled）を有効化します。
 */
@SpringBootApplication
@EnableScheduling
class MyappApplication

fun main(args: Array<String>) {
    runApplication<MyappApplication>(*args)
}
