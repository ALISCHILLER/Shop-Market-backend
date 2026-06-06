#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
SERVICE_NAME="${POSTGRES_SERVICE_NAME:-postgres}"
BACKUP_FILE="${1:-}"

if [ -z "${BACKUP_FILE}" ]; then
  echo "Usage: scripts/restore-postgres.sh <backup-file.dump>"
  exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
  echo "Backup file not found: ${BACKUP_FILE}"
  exit 1
fi

if [ ! -f ".env" ]; then
  echo "Missing .env file. Copy .env.prod.example to .env and set production values."
  exit 1
fi

set -a
source .env
set +a

: "${POSTGRES_DB:?POSTGRES_DB is required}"
: "${POSTGRES_USER:?POSTGRES_USER is required}"

echo "WARNING: This will restore backup into database '${POSTGRES_DB}'."
echo "Backup file: ${BACKUP_FILE}"
echo "Press ENTER to continue or Ctrl+C to cancel."
read -r _

echo "Restoring PostgreSQL backup..."

cat "${BACKUP_FILE}" | docker compose -f "${COMPOSE_FILE}" exec -T "${SERVICE_NAME}" \
  pg_restore \
    -U "${POSTGRES_USER}" \
    -d "${POSTGRES_DB}" \
    --clean \
    --if-exists \
    --no-owner \
    --no-acl

echo "Restore completed."