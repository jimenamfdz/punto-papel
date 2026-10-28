#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
pg_bin="${PG_BIN:-/opt/homebrew/opt/postgresql@17/bin}"
pg_data="$project_root/.local/postgres"

if [[ ! -x "$pg_bin/pg_ctl" ]]; then
  echo 'Instala PostgreSQL 17 o define PG_BIN con la ruta de sus ejecutables.' >&2
  exit 1
fi

case "${1:-start}" in
  start)
    mkdir -p "$project_root/.local"
    if [[ ! -f "$pg_data/PG_VERSION" ]]; then
      "$pg_bin/initdb" -D "$pg_data" --encoding=UTF8 --locale=en_US.UTF-8 \
        --auth-local=peer --auth-host=scram-sha-256
    fi
    if ! "$pg_bin/pg_ctl" -D "$pg_data" status >/dev/null 2>&1; then
      "$pg_bin/pg_ctl" -D "$pg_data" -l "$project_root/.local/postgres.log" \
        -o '-h localhost -p 5432' start
    fi
    ;;
  stop) "$pg_bin/pg_ctl" -D "$pg_data" stop -m fast ;;
  status) "$pg_bin/pg_ctl" -D "$pg_data" status ;;
  *) echo 'Uso: bash scripts/postgres-local.sh [start|stop|status]' >&2; exit 1 ;;
esac
