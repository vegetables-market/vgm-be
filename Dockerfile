# ビルドステージ
FROM gradle:8-jdk21-alpine AS build

WORKDIR /app

# Gradleキャッシュ用
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
RUN ./gradlew dependencies --no-daemon || true

# ソースコードをコピーしてビルド
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# 実行ステージ
# --- Build Stage ---
FROM gradle:8-jdk21 AS builder
WORKDIR /app
# 依存関係のキャッシュ効率化のため先にGradle関連をコピー
COPY build.gradle.kts settings.gradle.kts ./
# ソースコードをコピー
COPY src ./src
# ビルド実行 (テストは除外して高速化)
RUN gradle build --no-daemon -x test

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# ビルド成果物をコピー (ファイル名はプロジェクト設定によりますが、bootJarで生成されるものを探します)
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]