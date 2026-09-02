#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if java -version 2>&1 | grep --quiet 'version "26'; then
	for candidate in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-25-openjdk-amd64; do
		if [[ -x "$candidate/bin/java" ]]; then
			export JAVA_HOME="$candidate"
			export PATH="$JAVA_HOME/bin:$PATH"
			break
		fi
	done
fi

if curl --fail --silent http://localhost:8080/ | grep --quiet '<title>Archiv AI</title>'; then
	echo "AI RAG uz bezi na http://localhost:8080"
	exit 0
fi

export DATABASE_URL="jdbc:postgresql://localhost:5433/rag"
export DATABASE_USER="rag"
export DATABASE_PASSWORD="rag"
export OLLAMA_BASE_URL="http://localhost:11434"
export OLLAMA_CHAT_MODEL="qwen2.5:3b"
export OLLAMA_EMBEDDING_MODEL="bge-m3"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:--Xms512m -Xmx4g}"


OLLAMA_URL="${OLLAMA_BASE_URL:-http://localhost:11434}"
docker compose up -d --wait postgres
docker compose exec -T postgres psql -U "$DATABASE_USER" -d rag -c 'CREATE EXTENSION IF NOT EXISTS vector;'

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