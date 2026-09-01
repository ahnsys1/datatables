package cz.example.airag.document;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import cz.example.airag.config.OllamaModelManager;

@Service
public class DocumentService {

    private final VectorStore vectorStore;
    private final OllamaModelManager ollamaModelManager;
    private final OriginalDocumentStore originalDocumentStore;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    public DocumentService(VectorStore vectorStore, OllamaModelManager ollamaModelManager,
            OriginalDocumentStore originalDocumentStore) {
        this.vectorStore = vectorStore;
        this.ollamaModelManager = ollamaModelManager;
        this.originalDocumentStore = originalDocumentStore;
    }

    public UploadResult ingest(byte[] fileContent, String originalName, ProgressCallback progressCallback)
            throws IOException {
        String fileName = validateAndGetFileName(fileContent, originalName);
        String documentId = UUID.randomUUID().toString();
        ByteArrayResource resource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        List<Document> chunks = textSplitter.apply(new TikaDocumentReader(resource).get());
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("PDF neobsahuje zadny citelny text.");
        }

        chunks.forEach(chunk -> {
            chunk.getMetadata().put("documentId", documentId);
            chunk.getMetadata().put("fileName", fileName);
        });
        synchronized (ollamaModelManager) {
            ollamaModelManager.unloadChatModel();
            try {
                int batchSize = 8;
                for (int start = 0; start < chunks.size(); start += batchSize) {
                    int end = Math.min(start + batchSize, chunks.size());
                    vectorStore.add(chunks.subList(start, end));
                    progressCallback.accept((int) Math.round(end * 100.0 / chunks.size()));
                }
                originalDocumentStore.save(documentId, fileName, fileContent);
            } catch (RuntimeException | IOException exception) {
                try {
                    vectorStore.delete("documentId == '" + documentId + "'");
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
                throw exception;
            } finally {
                ollamaModelManager.unloadEmbeddingModel();
            }
        }

        return new UploadResult(documentId, fileName, chunks.size());
    }

    private String validateAndGetFileName(byte[] fileContent, String originalName) {
        if (fileContent.length == 0) {
            throw new IllegalArgumentException("Vyberte neprazdny PDF soubor.");
        }

        String fileName = originalName == null ? "document.pdf" : originalName.replaceAll("[\\r\\n]", "");
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Lze nahravat pouze PDF soubory.");
        }
        return fileName;
    }

    public record UploadResult(String documentId, String fileName, int chunks) {

    }

    @FunctionalInterface
    public interface ProgressCallback {

        void accept(int progress) throws IOException;
    }
}
