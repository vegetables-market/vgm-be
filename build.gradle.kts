buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-core:11.7.2")
        classpath("org.flywaydb:flyway-database-postgresql:11.7.2")
        classpath("org.postgresql:postgresql:42.7.3")
    }
}

plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
    kotlin("plugin.jpa") version "2.2.10"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}


group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "myapp"
extra["commons-lang3.version"] = "3.18.0"
extra["netty.version"] = "4.1.129.Final"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// Force commons-lang3 version at global level to override any transitive dependencies or BOMs
configurations.all {
    resolutionStrategy {
        force("org.apache.commons:commons-lang3:3.18.0")
        force("com.google.protobuf:protobuf-java:3.25.5")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-crypto")

    // H2 Database for testing
    testRuntimeOnly("com.h2database:h2")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // totp
    implementation("dev.samstevens.totp:totp:1.7.1")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.7.0") {
        exclude(group = "io.grpc", module = "grpc-netty-shaded")
    }
    implementation("io.grpc:grpc-netty:1.71.0")

    // Stripe SDK for payment processing
    implementation("com.stripe:stripe-java:26.3.0")



    // WebClient for PayPay API calls
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Dotenv for environment variable management
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // Dotenv-kotlin for custom MultiEnvFileLoader
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // OpenAPI (Swagger UI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    constraints {
        implementation("com.beust:jcommander:1.82") {
            because("Previous versions have security vulnerability WS-2019-0490")
        }
        implementation("com.google.guava:guava") {
            version {
                strictly("33.4.0-jre")
            }
            because("CVE-2023-2976, CVE-2020-8908: Vulnerability found in older versions.")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Docker tasks
tasks.register<Exec>("dockerUp") {
    description = "Start Docker containers using docker-compose"
    commandLine("docker-compose", "-f", "docker-compose.yml", "up", "-d", "--build")
}

tasks.register<Exec>("dockerDown") {
    description = "Stop Docker containers using docker-compose"
    commandLine("docker-compose", "-f", "docker-compose.yml", "down")
}

tasks.register<Exec>("dockerLogs") {
    description = "Show Docker logs"
    commandLine("docker-compose", "-f", "docker-compose.yml", "logs", "-f", "app")
}


tasks.register("flywayClean") {
    group = "flyway"
    description = "Cleans all objects in the configured Flyway schemas."

    doLast {
        val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/myapp"
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

        if (dbUrl.isBlank() || dbUser.isBlank() || dbPassword.isBlank()) {
            throw GradleException("Database credentials (DB_URL, DB_USER, DB_PASSWORD) must be set as environment variables or in .env.local for flywayClean task.")
        }

        val flyway = org.flywaydb.core.Flyway.configure()
            .dataSource(dbUrl, dbUser, dbPassword)
            .locations("classpath:db/migration") // Ensure this matches application.yml
            .cleanDisabled(false) // Add this line
            .load()

        flyway.clean()
        println("Flyway clean successful! All objects in the schema have been removed.")
    }
}

tasks.register("flywayRepair") {
    group = "flyway"
    description = "Repairs the Flyway schema history table."

    doLast {
        val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/myapp"
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

        if (dbUrl.isBlank() || dbUser.isBlank() || dbPassword.isBlank()) {
            throw GradleException("Database credentials (DB_URL, DB_USER, DB_PASSWORD) must be set as environment variables or in .env.local for flywayRepair task.")
        }

        val flyway = org.flywaydb.core.Flyway.configure()
            .dataSource(dbUrl, dbUser, dbPassword)
            .locations("filesystem:src/main/resources/db/migration") // Use filesystem path to ensure files are found
            .baselineOnMigrate(true) // Should match application.yml
            .baselineVersion("0") // Should match application.yml
            .load()

        flyway.repair()
        println("Flyway repair successful!")
    }
}
