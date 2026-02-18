#!/bin/sh
set -e

# 改行コード対策: 環境変数の末尾に \r があったら削除
IS_TAILSCALE="$(printf '%s' "${IS_TAILSCALE:-true}" | tr -d '\r')"
TAILSCALE_AUTH_KEY="$(printf '%s' "${TAILSCALE_AUTH_KEY:-}" | tr -d '\r')"

echo "=== STARTING CONTAINER ==="

if [ "$IS_TAILSCALE" = "true" ]; then
  echo "--- Mode: TAILSCALE (Direct DB access via Tailscale network) ---"

  if [ -z "$TAILSCALE_AUTH_KEY" ]; then
    echo "!!! ERROR: TAILSCALE_AUTH_KEY is empty"
    exit 1
  fi

  # 1. Tailscale起動（Cloud Runでは userspace が正解）
  echo "Starting Tailscale..."
  tailscaled --tun=userspace-networking &

  # hostname は固定しない（Cloud Runで同時起動すると衝突しがち）
  TS_HOSTNAME="cloudrun-${K_SERVICE:-svc}-${K_REVISION:-rev}-${HOSTNAME:-inst}"

  # 2. ログイン待機（ephemeral + tag）
  tailscale version || true
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
else
  echo "--- Mode: LOCAL ---"
  echo "Skipping Tailscale setup."
fi

# 3. アプリ起動
echo "Starting Java App..."
exec "$@"
