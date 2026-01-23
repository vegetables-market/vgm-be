#!/bin/sh
set -e

# 改行コード対策: 環境変数の末尾に \r があったら削除
TAILSCALE_AUTH_KEY=$(echo "$TAILSCALE_AUTH_KEY" | tr -d '\r')
TAILSCALE_VGM_DB_HOST=$(echo "$TAILSCALE_VGM_DB_HOST" | tr -d '\r')

echo "=== STARTING CLOUD RUN CONTAINER ==="

if [ "$IS_LOCAL" = "false" ]; then

  echo "Target DB IP: '${TAILSCALE_VGM_DB_HOST}'"

  # 1. Tailscale起動
  echo "Starting Tailscale..."
  tailscaled --tun=userspace-networking --socks5-server=localhost:1055 &

  # 2. ログイン待機
  until tailscale up --authkey=${TAILSCALE_AUTH_KEY} --hostname=cloudrun-app; do
      sleep 1
  done
  echo "Tailscale is UP!"

  # 3. トンネル作成 (ログをファイルに出力してエラーを見る)
  echo "Starting Socat Tunnel..."
  # -d -d -d で詳細ログを出す
  socat -d -d -d TCP-LISTEN:5432,fork,bind=127.0.0.1 SOCKS5:127.0.0.1:$TAILSCALE_VGM_DB_HOST:5432,socksport=1055 > /var/log/socat.log 2>&1 &
  SOCAT_PID=$!

  # 少し待って、socatが生きているか確認
  sleep 2
  if ! kill -0 $SOCAT_PID > /dev/null 2>&1; then
      echo "!!! ERROR: Socat died immediately! Check logs below:"
      cat /var/log/socat.log
      exit 1
  else
      echo "Socat is running (PID: $SOCAT_PID)."
  fi
else
  echo "--- Local Mode: OFF (Local Development) ---"
  echo "Skipping Tailscale setup. Connecting to local network DB."
fi

# 4. アプリ起動
echo "Starting Java App..."
exec "$@"