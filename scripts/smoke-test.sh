#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8282}"
TOKEN=$(curl -sS -X POST "$BASE_URL/api/v1/User/loginUser" \
  -H 'Content-Type: application/json' \
  -d '{"customerCode":"1001","password":"123456"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then
  echo "Login failed"
  exit 1
fi
curl -fsS "$BASE_URL/actuator/health" >/dev/null
curl -fsS "$BASE_URL/api/v1/Product/GetListKala" >/dev/null
curl -fsS "$BASE_URL/api/v1/User/CustomerProfile" -H "Authorization: Bearer $TOKEN" >/dev/null
echo "Smoke test passed"
