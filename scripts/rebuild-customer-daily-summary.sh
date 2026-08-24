#!/usr/bin/env bash

set -Eeuo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd -- "${script_dir}/.." && pwd)"
env_file="${project_dir}/.env"
sql_file="${script_dir}/rebuild-customer-daily-summary.sql"

if [[ ! -f "$env_file" ]]; then
  echo "Erro: arquivo .env não encontrado em ${project_dir}" >&2
  echo "Crie-o a partir de .env.example antes de executar este script." >&2
  exit 1
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "Erro: comando psql não encontrado." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-espresso-dev}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"

echo "Reconstruindo customer_daily_summary em ${DB_HOST}:${DB_PORT}/${DB_NAME}..."

PGPASSWORD="$DB_PASSWORD" psql \
  --host "$DB_HOST" \
  --port "$DB_PORT" \
  --username "$DB_USER" \
  --dbname "$DB_NAME" \
  --set ON_ERROR_STOP=1 \
  < "$sql_file"

echo "Resumo diário reconstruído com sucesso."
