#!/bin/sh

# 1. Tailscaleのデーモンをバックグラウンドで起動（ユーザーモードネットワーキング）
# Cloud Runでは /dev/net/tun が使えないため、userspace-networkingを使用
tailscaled --tun=userspace-networking --socks5-server=localhost:1055 &

# 2. Tailscaleにログイン
# Untilループを使って、デーモンが起きるのを少し待ってからログイン試行
until tailscale up --authkey=${TAILSCALE_AUTH_KEY} --hostname=cloudrun-app; do
    sleep 1
done

echo "Tailscale started"

# 3. DBへのトンネルを作成 (socat)
# Cloud Run内の「localhost:5432」へのアクセスを、
# Tailscale(SOCKS5)経由で「100.x.y.z:5432」に転送する
# ★重要: 下の 100.x.y.z は実際のDBサーバーのIPに変えてください
DB_TARGET_IP="${TAILSCALE_VGM_DB_HOST}"

socat TCP-LISTEN:5432,fork,bind=127.0.0.1 SOCKS5:127.0.0.1:$DB_TARGET_IP:5432,socksport=1055 &

echo "DB Tunnel started"

# 4. アプリケーションを起動
# (Dockerfileの CMD に書いてあったコマンドをここに書く)
# 例: Spring Bootなら java -jar ..., Pythonなら uvicorn ...
# 以下の "$@" は Dockerfileの CMD で渡された引数をそのまま実行する魔法の変数
exec "$@"