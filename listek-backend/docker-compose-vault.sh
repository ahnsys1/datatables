#!/bin/sh
set -eu

VAULT_CONTAINER="${VAULT_CONTAINER:-vault}"

if ! docker inspect "$VAULT_CONTAINER" >/dev/null 2>&1; then
  echo "Vault container '$VAULT_CONTAINER' is not running" >&2
  exit 1
fi

LISTEK_VAULT_TOKEN="$(docker logs "$VAULT_CONTAINER" 2>&1 | awk '/Root Token:/{token=$3} END{print token}')"

if [ -z "$LISTEK_VAULT_TOKEN" ]; then
  echo "Could not obtain the development Vault token" >&2
  exit 1
fi

if ! docker exec \
  -e VAULT_ADDR=http://127.0.0.1:8200 \
  -e VAULT_TOKEN="$LISTEK_VAULT_TOKEN" \
  "$VAULT_CONTAINER" vault token lookup >/dev/null 2>&1; then
  echo "The development Vault token is invalid" >&2
  exit 1
fi

export LISTEK_VAULT_TOKEN
exec docker compose "$@"