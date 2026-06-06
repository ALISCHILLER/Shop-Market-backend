#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
SERVICE_NAME="${POSTGRES_SERVICE_NAME:-postgres}"
BACKUP_DIR="${BACKUP_DIR:-backups}"

TIMESTAMP="$(date -u +'%Y%m%dT%H%M%SZ')"
OUTPUT_FILE="${BACKUP_DIR}/eshop-${TIMESTAMP}.dump"

mkdir -p "${BACKUP_DIR}"

if [ ! -f ".env" ]; then
  echo "Missing .env file. Copy .env.prod.example to .env and set production values."
  exit 1
fi

set -a
source .env
set +a

: "${POSTGRES_DB:?POSTGRES_DB is required}"
: "${POSTGRES_USER:?POSTGRES_USER is required}"

echo "Creating PostgreSQL backup..."
echo "Compose file: ${COMPOSE_FILE}"
echo "Database: ${POSTGRES_DB}"
echo "Output: ${OUTPUT_FILE}"

docker compose -f "${COMPOSE_FILE}" exec -T "${SERVICE_NAME}" \
  pg_dump \
    -U "${POSTGRES_USER}" \
    -d "${POSTGRES_DB}" \
    -Fc \
    --no-owner \
    --no-acl \
  > "${OUTPUT_FILE}"

echo "Backup completed: ${OUTPUT_FILE}"