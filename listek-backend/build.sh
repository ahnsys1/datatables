#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORKSPACE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "running listek-frontend build"
cd "$WORKSPACE_DIR/listek-frontend"
npm run build

cd "$WORKSPACE_DIR/listek-admin"
mvn clean install -DskipTests

cd "$SCRIPT_DIR"
mvn clean install -DskipTests

echo "Starting Docker Compose services with rebuild..."
read -rsp 'Vault token: ' VAULT_TOKEN
echo
export VAULT_TOKEN
trap 'unset VAULT_TOKEN' EXIT
sh "$SCRIPT_DIR/docker-compose-vault.sh" up --build
