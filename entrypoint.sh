#!/bin/sh
set -eu

# 改行コード対策
IS_TAILSCALE="$(printf '%s' "${IS_TAILSCALE:-true}" | tr -d '\r')"
TAILSCALE_AUTH_KEY="$(printf '%s' "${TAILSCALE_AUTH_KEY:-}" | tr -d '\r')"
TAILSCALE_VGM_DB_HOST="$(printf '%s' "${TAILSCALE_VGM_DB_HOST:-}" | tr -d '\r')"

echo "=== STARTING CONTAINER ==="

if [ "$IS_TAILSCALE" = "true" ]; then
  echo "--- Mode: TAILSCALE (userspace + SOCKS5 + socat tunnel) ---"

  if [ -z "$TAILSCALE_AUTH_KEY" ]; then
    echo "!!! ERROR: TAILSCALE_AUTH_KEY is empty"
    exit 1
  fi
  if [ -z "$TAILSCALE_VGM_DB_HOST" ]; then
    echo "!!! ERROR: TAILSCALE_VGM_DB_HOST is empty (100.x.y.z expected)"
    exit 1
  fi

  echo "Starting tailscaled (userspace networking + SOCKS5)..."
  mkdir -p /var/run/tailscale /var/lib/tailscale

  tailscaled \
    --state=mem: \
    --socket=/var/run/tailscale/tailscaled.sock \
    --tun=userspace-networking \
    --socks5-server=127.0.0.1:1055 \
    >/dev/stdout 2>/dev/stderr &

  # tailscaled 起動待ち
  for i in $(seq 1 50); do
    tailscale --socket=/var/run/tailscale/tailscaled.sock status >/dev/null 2>&1 && break
    sleep 0.1
  done

  TS_HOSTNAME="cloudrun-${K_SERVICE:-svc}-${K_REVISION:-rev}-${HOSTNAME:-inst}"
  echo "tailscale up... hostname=$TS_HOSTNAME"

  # ※ netfilter-mode=off は userspace で安定しやすい
  # ※ --advertise-tags はACL側で許可されている必要あり
  tailscale --socket=/var/run/tailscale/tailscaled.sock up \
    --authkey="$TAILSCALE_AUTH_KEY" \
    --hostname="$TS_HOSTNAME" \
    --accept-dns=false \
    --advertise-tags=tag:gcp \
    --netfilter-mode=off

  echo "Tailscale is UP. Starting socat tunnel: 127.0.0.1:5432 -> ${TAILSCALE_VGM_DB_HOST}:5432 (via SOCKS5 127.0.0.1:1055)"

  command -v socat >/dev/null 2>&1 || { echo "!!! ERROR: socat not installed"; exit 1; }
  command -v nc    >/dev/null 2>&1 || { echo "!!! ERROR: nc not installed"; exit 1; }


  # socat: localhost:5432 を LISTEN して、SOCKS5 経由で 100.x のDBへ
  socat -d -d \
    TCP-LISTEN:5432,fork,reuseaddr \
    SOCKS5:127.0.0.1:${TAILSCALE_VGM_DB_HOST}:5432,socksport=1055 \
    >/dev/stdout 2>/dev/stderr &

  # 5432 LISTEN 待ち（busybox-extras の nc が必要）
  for i in $(seq 1 50); do
    nc -z 127.0.0.1 5432 >/dev/null 2>&1 && break
    sleep 0.1
  done

  if ! nc -z 127.0.0.1 5432 >/dev/null 2>&1; then
    echo "!!! ERROR: socat did not open 127.0.0.1:5432"
    exit 1
  fi

  DB_NAME="$(printf '%s' "${DB_NAME:-vgm_db_dev}" | tr -d '\r')"
  export DB_URL="jdbc:postgresql://127.0.0.1:5432/${DB_NAME}"
  echo "DB_URL=${DB_URL}"

else
  echo "--- Mode: LOCAL ---"
  echo "Skipping Tailscale setup."
fi

echo "Starting Java App..."
exec "$@"
