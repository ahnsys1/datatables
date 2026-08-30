#!/bin/sh
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://127.0.0.1:8200}"
VAULT_TOKEN="${VAULT_TOKEN:?VAULT_TOKEN must be set}"
VAULT_SECRET_PATH="${VAULT_SECRET_PATH:-secrets/data/datatables}"

command -v curl >/dev/null 2>&1 || { echo "curl is required" >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "jq is required" >&2; exit 1; }

vault_response="$(curl --fail --silent --show-error \
  --header "X-Vault-Token: ${VAULT_TOKEN}" \
  "${VAULT_ADDR}/v1/${VAULT_SECRET_PATH}")"

read_secret() {
  printf '%s' "${vault_response}" | jq --exit-status --raw-output ".data.data.$1 | strings | select(length > 0)"
}

export DATATABLES_DB_NAME="$(read_secret DB_NAME)"
export DATATABLES_DB_USERNAME="$(read_secret DB_USERNAME)"
export DATATABLES_DB_PASSWORD="$(read_secret DB_PASSWORD)"
export DATATABLES_RABBITMQ_USERNAME="$(read_secret RABBITMQ_USERNAME)"
export DATATABLES_RABBITMQ_PASSWORD="$(read_secret RABBITMQ_PASSWORD)"
export DATATABLES_KEYCLOAK_ADMIN_USERNAME="$(read_secret KEYCLOAK_ADMIN_USERNAME)"
export DATATABLES_KEYCLOAK_ADMIN_PASSWORD="$(read_secret KEYCLOAK_ADMIN_PASSWORD)"

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec docker compose -f "${script_dir}/docker-compose.yml" "$@"