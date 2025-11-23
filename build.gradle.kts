plugins {
	kotlin("jvm") version "2.2.10"
	kotlin("plugin.spring") version "2.2.10"
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
    runtimeOnly("org.postgresql:postgresql")
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
