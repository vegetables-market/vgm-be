#!/bin/sh
set -e

echo "=== STARTING CLOUD RUN CONTAINER ==="

# 1. 環境変数のチェック
if [ -z "$TAILSCALE_AUTH_KEY" ]; then
  echo "ERROR: TAILSCALE_AUTH_KEY is not set!"
  exit 1
fi
if [ -z "$TAILSCALE_VGM_DB_HOST" ]; then
  echo "ERROR: TAILSCALE_VGM_DB_HOST is not set!"
  echo "Hint: Did you set the GitHub Secret and mapping in deploy.yaml?"
  exit 1
fi

echo "Target DB IP is: $TAILSCALE_VGM_DB_HOST"

# 2. Tailscale起動
echo "Starting Tailscale daemon..."
tailscaled --tun=userspace-networking --socks5-server=localhost:1055 &

# 3. Tailscaleログイン待機
echo "Waiting for Tailscale login..."
until tailscale up --authkey=${TAILSCALE_AUTH_KEY} --hostname=cloudrun-app; do
    sleep 1
    echo "Retrying tailscale up..."
done
echo "Tailscale is UP!"

# 4. トンネル作成 (socat)
echo "Starting Socat Tunnel..."
# ログにエラーが出るように -d -d オプションを追加しても良いが、まずはシンプルに
socat TCP-LISTEN:5432,fork,bind=127.0.0.1 SOCKS5:127.0.0.1:$TAILSCALE_VGM_DB_HOST:5432,socksport=1055 &
PID_SOCAT=$!

# socatが即死していないか1秒待って確認
sleep 2
if ! kill -0 $PID_SOCAT > /dev/null 2>&1; then
    echo "ERROR: Socat process died! Check if IP address is correct."
    exit 1
fi
echo "DB Tunnel started on localhost:5432"

# 5. アプリ起動
echo "Starting Java App..."
exec "$@"