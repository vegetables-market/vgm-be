#!/bin/sh
set -e

# 改行コード対策: 環境変数の末尾に \r があったら削除
IS_TAILSCALE="$(printf '%s' "${IS_TAILSCALE:-true}" | tr -d '\r')"
TAILSCALE_AUTH_KEY="$(printf '%s' "${TAILSCALE_AUTH_KEY:-}" | tr -d '\r')"
TAILSCALE_VGM_DB_HOST="$(printf '%s' "${TAILSCALE_VGM_DB_HOST:-}" | tr -d '\r')"

echo "=== STARTING CONTAINER ==="

if [ "$IS_TAILSCALE" = "true" ]; then
  echo "--- Mode: TAILSCALE (Connecting to Remote DB via Tailscale) ---"

  if [ -z "$TAILSCALE_AUTH_KEY" ]; then
    echo "!!! ERROR: TAILSCALE_AUTH_KEY is empty"
    exit 1
  fi

  if [ -z "$TAILSCALE_VGM_DB_HOST" ]; then
    echo "!!! ERROR: TAILSCALE_VGM_DB_HOST is empty"
    exit 1
  fi

  echo "Target DB IP: '${TAILSCALE_VGM_DB_HOST}'"

  # 1. Tailscale起動（Cloud Runでは userspace が正解）
  echo "Starting Tailscale..."
  tailscaled --tun=userspace-networking --socks5-server=localhost:1055 &

  # hostname は固定しない（Cloud Runで同時起動すると衝突しがち）
  TS_HOSTNAME="cloudrun-${K_SERVICE:-svc}-${K_REVISION:-rev}-${HOSTNAME:-inst}"

  # 2. ログイン待機（ephemeral + tag）
  until tailscale up \
    --authkey="$TAILSCALE_AUTH_KEY" \
    --hostname="$TS_HOSTNAME" \
    --ephemeral \
    --accept-dns=false \
    --advertise-tags=tag:gcp
  do
    sleep 1
  done

  echo "Tailscale is UP! hostname=$TS_HOSTNAME"

  # 3. SOCKS5 経由で localhost:5432 に DB を転送
  echo "Starting Socat Tunnel..."
  socat -d -d -d \
    TCP-LISTEN:5432,fork,bind=127.0.0.1,reuseaddr \
    SOCKS5:127.0.0.1:"$TAILSCALE_VGM_DB_HOST":5432,socksport=1055 \
    > /var/log/socat.log 2>&1 &

  SOCAT_PID=$!

  sleep 2
  if ! kill -0 "$SOCAT_PID" > /dev/null 2>&1; then
    echo "!!! ERROR: Socat died immediately! Check logs below:"
    cat /var/log/socat.log || true
    exit 1
  else
    echo "Socat is running (PID: $SOCAT_PID)."
  fi
else
  echo "--- Mode: LOCAL (Connecting to Local Network DB) ---"
  echo "Skipping Tailscale setup."
fi

echo "Starting Java App..."
exec "$@"
