# --- Build Stage ---
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app

# Gradle キャッシュ効率化
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

RUN apk add --no-cache ca-certificates socat busybox-extras curl

# Tailscale（edge/community）
RUN apk add --no-cache --repository=https://dl-cdn.alpinelinux.org/alpine/edge/community tailscale

# entrypoint.sh をコピー（改行コード対策 + 実行権限）
COPY entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

# ビルド成果物をコピー
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]

