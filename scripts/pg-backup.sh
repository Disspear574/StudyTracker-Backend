#!/usr/bin/env bash
set -euo pipefail

PG_LXC="${PG_LXC:-104}"
DB_NAME="${DB_NAME:-studytracker}"
MC_ALIAS="${MC_ALIAS:-minio}"
BUCKET="${BUCKET:-studytracker-backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

STAMP="$(date +%Y%m%d-%H%M%S)"
FILE="studytracker-${STAMP}.sql.gz"

pct exec "$PG_LXC" -- su postgres -c "pg_dump ${DB_NAME}" | gzip > "/tmp/${FILE}"
mc cp "/tmp/${FILE}" "${MC_ALIAS}/${BUCKET}/${FILE}"
rm -f "/tmp/${FILE}"

mc rm --recursive --force --older-than "${RETENTION_DAYS}d" "${MC_ALIAS}/${BUCKET}/" || true

echo "backup done: ${FILE}"
