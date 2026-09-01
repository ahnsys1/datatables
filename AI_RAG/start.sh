#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if curl --fail --silent http://localhost:8080/ | grep --quiet '<title>Archiv AI</title>'; then
	echo "AI RAG uz bezi na http://localhost:8080"
	exit 0
fi

OLLAMA_URL="${OLLAMA_BASE_URL:-http://localhost:11434}"
docker compose up -d postgres

if curl --fail --silent "$OLLAMA_URL/api/tags" >/dev/null; then
	echo "Pouzivam jiz bezici Ollamu na $OLLAMA_URL"
	curl --fail --silent --show-error "$OLLAMA_URL/api/pull" \
		--header "Content-Type: application/json" \
		--data '{"name":"qwen2.5:3b","stream":false}' >/dev/null
	curl --fail --silent --show-error "$OLLAMA_URL/api/pull" \
		--header "Content-Type: application/json" \
		--data '{"name":"bge-m3","stream":false}' >/dev/null
else
	docker compose up -d ollama
	docker compose exec ollama ollama pull qwen2.5:3b
	docker compose exec ollama ollama pull bge-m3
fi

mvn spring-boot:run