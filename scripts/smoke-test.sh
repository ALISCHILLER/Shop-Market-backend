#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8282}"

LOGIN_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"customerCode":"1001","password":"123456"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
  echo "Login failed"
  echo "$LOGIN_RESPONSE"
  exit 1
fi

curl -fsS "$BASE_URL/actuator/health" >/dev/null
curl -fsS "$BASE_URL/api/v1/products" >/dev/null
curl -fsS "$BASE_URL/api/v1/product-categories" >/dev/null
curl -fsS "$BASE_URL/api/v1/banners" >/dev/null

curl -fsS "$BASE_URL/api/v1/auth/me" \
  -H "Authorization: Bearer $TOKEN" >/dev/null

curl -fsS "$BASE_URL/api/v1/cart/payment-terms" \
  -H "Authorization: Bearer $TOKEN" >/dev/null

echo "Smoke test passed"