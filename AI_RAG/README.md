# AI RAG

Lokální český chat nad PDF dokumenty postavený na Spring Boot, Spring AI, Ollama a PostgreSQL s rozšířením pgvector.

Databázová služba používá PostgreSQL 18 s pgvector. Data z dřívější PG17 instalace byla migrována přes logický dump; původní Docker volume `ai_rag_pgvector-data` zůstává zachovaný pro případný rollback.

## Požadavky

- Java 21
- Maven 3.9+
- Docker s pluginem Compose
- alespoň 8 GB RAM

## Rychlé spuštění

```bash
chmod +x start.sh
./start.sh
```

Skript umí použít Ollamu, která již běží na `localhost:11434`; jinak ji spustí v Dockeru. První spuštění stáhne modely `qwen2.5:3b` a `bge-m3`, takže může trvat několik minut. Poté otevřete <http://localhost:8080>.

Pokud aplikace již běží, opakované spuštění skriptu pouze vypíše její adresu. Běžící Spring Boot ukončíte pomocí `Ctrl+C` v terminálu, ve kterém byl spuštěn.

## Ruční spuštění

```bash
docker compose up -d postgres ollama
docker compose exec ollama ollama pull qwen2.5:3b
docker compose exec ollama ollama pull bge-m3
mvn spring-boot:run
```

Celou aplikaci lze sestavit a spustit také v Dockeru:

```bash
docker compose up -d postgres ollama
docker compose exec ollama ollama pull qwen2.5:3b
docker compose exec ollama ollama pull bge-m3
docker compose --profile full up --build app
```

## Použití

1. V levém panelu nahrajte jedno nebo více PDF.
2. Během zpracování se u dokumentu zobrazuje skutečný procentní průběh indexace po dávkách.
3. Položte otázku do chatu. Odpověď obsahuje názvy použitých dokumentů.
4. Tlačítkem **Nový chat** vymažete konverzační kontext. Indexované dokumenty zůstanou v databázi.

Lokální indexace většího PDF může podle výkonu počítače trvat několik minut. Indexace běží jako serverová úloha a stránka průběžně načítá její stav, takže ji neukončí timeout dlouhého HTTP spojení.

Textové PDF funguje přímo. Naskenované PDF bez textové vrstvy vyžaduje OCR, které tato základní verze neprovádí.

## Konfigurace

Nastavení lze měnit proměnnými prostředí:

| Proměnná | Výchozí hodnota |
| --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/rag` |
| `DATABASE_USER` | `rag` |
| `DATABASE_PASSWORD` | `rag` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` |
| `OLLAMA_CHAT_MODEL` | `qwen2.5:3b` |
| `OLLAMA_EMBEDDING_MODEL` | `bge-m3` |

Model `bge-m3` vytváří vektory o 1024 rozměrech. Při změně embedding modelu upravte také `dimensions` v `application.yml` a vytvořte čistý databázový svazek.

Embedding model se po každém použití uvolní z paměti, aby se na počítači s 8 GB RAM mohl načíst chat model. Kvůli přepínání modelů může první odpověď trvat déle.