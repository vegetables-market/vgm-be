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
	// Spring devtools
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	//	ページ追加、レイアウト追加
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation ("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// WebJars for Bootstrap and FontAwesome
	implementation("org.webjars:webjars-locator-core")
	implementation("org.webjars:font-awesome:6.4.2")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
