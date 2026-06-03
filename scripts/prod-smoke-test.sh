#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8282}"

echo "Checking health..."
curl -fsS "$BASE_URL/actuator/health" >/dev/null

echo "Checking public catalog..."
curl -fsS "$BASE_URL/api/v1/products?page=0&size=1" >/dev/null
curl -fsS "$BASE_URL/api/v1/product-categories" >/dev/null
curl -fsS "$BASE_URL/api/v1/banners" >/dev/null

echo "Checking auth endpoint validation..."
STATUS=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"customerCode":"wrong","password":"wrong"}')

if [ "$STATUS" != "401" ] && [ "$STATUS" != "400" ]; then
  echo "Unexpected auth validation status: $STATUS"
  exit 1
fi

echo "Production smoke test passed"