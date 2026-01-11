# --- Build Stage ---
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app

# Gradle キャッシュの効率化
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# ソースコードをコピーしてビルド
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 1. 必要なツールをインストール
RUN apk update && apk add --no-cache curl socat ca-certificates iptables

# 2. Tailscaleのインストール
RUN apk add --no-cache tailscale

# 3. entrypoint.sh をコピー
COPY entrypoint.sh /entrypoint.sh

# ★ここを追加！ (sedで改行コード \r を削除して、実行権限をつける)
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

# 4. ビルド成果物をコピー
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
