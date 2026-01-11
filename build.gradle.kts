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

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation ("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
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
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.security:spring-security-crypto")
    
    // H2 Database for testing
    testRuntimeOnly("com.h2database:h2")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("dev.samstevens.totp:totp:1.7.1")

    // Stripe SDK for payment processing
    implementation("com.stripe:stripe-java:26.3.0")

    // WebClient for PayPay API calls
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Dotenv for environment variable management
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // Dotenv-kotlin for custom MultiEnvFileLoader
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Docker tasks
tasks.register("dockerUp") {
	description = "Start Docker containers using docker-compose"
	doLast {
		exec {
			commandLine("docker-compose", "-f", "docker-compose.yml", "up", "-d", "--build")
		}
	}
}

tasks.register("dockerDown") {
	description = "Stop Docker containers using docker-compose"
	doLast {
		exec {
			commandLine("docker-compose", "-f", "docker-compose.yml", "down")
		}
	}
}

tasks.register("dockerLogs") {
	description = "Show Docker logs"
	doLast {
		exec {
			commandLine("docker-compose", "-f", "docker-compose.yml", "logs", "-f", "app")
		}
	}
}
