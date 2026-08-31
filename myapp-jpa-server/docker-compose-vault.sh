#!/bin/sh
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://127.0.0.1:8200}"
VAULT_TOKEN="${VAULT_TOKEN:?VAULT_TOKEN must be set}"
VAULT_SECRET_PATH="${VAULT_SECRET_PATH:-secret/data/datatables}"

command -v curl >/dev/null 2>&1 || { echo "curl is required" >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "jq is required" >&2; exit 1; }

response_file="$(mktemp)"
trap 'rm -f "$response_file"' EXIT

status_code="$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' \
  --header "X-Vault-Token: ${VAULT_TOKEN}" \
  "${VAULT_ADDR}/v1/${VAULT_SECRET_PATH}")"

case "$status_code" in
  200) ;;
  404)
    echo "Vault secret '${VAULT_SECRET_PATH}' does not exist" >&2
    echo "Create it in the current Vault instance before starting Compose" >&2
    exit 1
    ;;
  403)
    echo "Vault denied access to '${VAULT_SECRET_PATH}'; check VAULT_TOKEN" >&2
    exit 1
    ;;
  *)
    echo "Vault request for '${VAULT_SECRET_PATH}' failed with HTTP ${status_code}" >&2
    exit 1
    ;;
esac

vault_response="$(cat "$response_file")"

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
echo ${'script_dir'}
exec docker compose -f "${script_dir}/docker-compose.yml" "$@"
