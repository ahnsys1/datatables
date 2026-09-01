package cz.example.airag.document;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

@Repository
public class OriginalDocumentStore {

    private final JdbcTemplate jdbcTemplate;

    public OriginalDocumentStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void initialize() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS source_documents (
                    document_id UUID PRIMARY KEY,
                    file_name TEXT NOT NULL,
                    content BYTEA NOT NULL,
                    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    public void save(String documentId, String fileName, byte[] content) {
        jdbcTemplate.update("""
                INSERT INTO source_documents (document_id, file_name, content)
                VALUES (?, ?, ?)
                ON CONFLICT (document_id) DO UPDATE
                SET file_name = EXCLUDED.file_name, content = EXCLUDED.content
                """, UUID.fromString(documentId), fileName, content);
    }

    public Optional<StoredDocument> find(String documentId) {
        UUID id;
        try {
            id = UUID.fromString(documentId);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        return jdbcTemplate.query(
                "SELECT file_name, content FROM source_documents WHERE document_id = ?",
                resultSet -> resultSet.next()
                ? Optional.of(new StoredDocument(resultSet.getString("file_name"), resultSet.getBytes("content")))
                : Optional.empty(),
                id);
    }

    public record StoredDocument(String fileName, byte[] content) {

    }
}
