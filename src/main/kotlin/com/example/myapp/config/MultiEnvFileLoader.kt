package com.example.myapp.config

//import io.github.cdimascio.dotenv.Dotenv
//import io.github.cdimascio.dotenv.DotenvException
//import org.springframework.boot.SpringApplication
//import org.springframework.boot.env.EnvironmentPostProcessor
//import org.springframework.core.env.ConfigurableEnvironment
//import org.springframework.core.env.PropertiesPropertySource
//import java.io.File
//import java.util.*
//
///**
// * 複数の.envファイルを読み込むカスタムEnvironmentPostProcessor
// *
// * 読み込むファイル:
// * - .env.db
// * - .env.mail
// * - .env.payment
// * - .env.cors
// * - .env.marketplace
// */
//class MultiEnvFileLoader : EnvironmentPostProcessor {
//
//    companion object {
//        private val ENV_FILES = listOf(
//            ".env.db",
//            ".env.mail",
//            ".env.payment",
//            ".env.cors",
//            ".env.marketplace"
//        )
//    }
//
//    override fun postProcessEnvironment(
//        environment: ConfigurableEnvironment,
//        application: SpringApplication
//    ) {
//        val baseDir = File(System.getProperty("user.dir"))
//
//        ENV_FILES.forEach { fileName ->
//            val envFile = File(baseDir, fileName)
//            if (envFile.exists()) {
//                try {
//                    val dotenv = Dotenv.configure()
//                        .directory(baseDir.absolutePath)
//                        .filename(fileName)
//                        .ignoreIfMissing()
//                        .load()
//
//                    val properties = Properties()
//                    dotenv.entries().forEach { entry ->
//                        properties[entry.key] = entry.value
//                    }
//
//                    val propertySource = PropertiesPropertySource(fileName, properties)
//                    environment.propertySources.addLast(propertySource)
//
//                    println("✓ Loaded: $fileName")
//                } catch (e: DotenvException) {
//                    println("⚠ Warning: Could not load $fileName - ${e.message}")
//                }
//            } else {
//                println("ℹ Info: $fileName not found, skipping")
//            }
//        }
//    }
//}
