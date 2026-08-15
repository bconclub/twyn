#!/usr/bin/env bash
# Run ON the VPS from the repo root: bash deploy/deploy.sh
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "deploy/.env missing. Copy .env.example and fill it in." >&2
  exit 1
fi

export TWIN_DOMAIN="$(grep ^TWIN_DOMAIN= .env | cut -d= -f2)"
docker compose up -d --build
docker compose ps
echo "TWIN live at https://${TWIN_DOMAIN}"
