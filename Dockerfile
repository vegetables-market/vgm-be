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

# 1. 必要なツールをインストール (Alpineなので apk を使用)
# ca-certificatesはHTTPS通信(Tailscaleや外部API)に必須
RUN apk update && apk add --no-cache curl socat ca-certificates iptables

# 2. Tailscaleのインストール
RUN curl -fsSL https://tailscale.com/install.sh | sh

# 3. entrypoint.sh をコピーして実行権限を付与
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# ビルド成果物をコピー
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]