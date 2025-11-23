# --- Build Stage ---
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app

# Gradle キャッシュの効率化
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
RUN ./gradlew dependencies --no-daemon || true

# ソースコードをコピーしてビルド
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# ビルド成果物をコピー
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]