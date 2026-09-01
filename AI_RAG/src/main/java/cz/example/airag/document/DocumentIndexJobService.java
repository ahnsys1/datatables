package cz.example.airag.document;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DocumentIndexJobService {

    private final DocumentService documentService;
    private final Executor documentIndexExecutor;
    private final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();

    public DocumentIndexJobService(
            DocumentService documentService,
            @Qualifier("documentIndexExecutor") Executor documentIndexExecutor) {
        this.documentService = documentService;
        this.documentIndexExecutor = documentIndexExecutor;
    }

    public String start(byte[] fileContent, String fileName) {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, new JobStatus(jobId, "queued", 0, "Čeká na indexaci…", null, fileName, 0,
                null, null, 0, null));
        documentIndexExecutor.execute(() -> index(jobId, fileContent, fileName));
        return jobId;
    }

    public JobStatus getStatus(String jobId) {
        JobStatus status = jobs.get(jobId);
        if (status == null || status.startedAt() == null) {
            return status;
        }

        long now = status.finishedAt() == null ? System.currentTimeMillis() : status.finishedAt();
        long elapsedSeconds = Math.max(0, (now - status.startedAt()) / 1000);
        Long estimatedCompletionAt = null;
        if (status.state().equals("complete")) {
            estimatedCompletionAt = status.finishedAt();
        } else if (status.progress() > 0) {
            long estimatedDuration = (now - status.startedAt()) * 100L / status.progress();
            estimatedCompletionAt = status.startedAt() + estimatedDuration;
        }
        return new JobStatus(status.jobId(), status.state(), status.progress(), status.message(),
                status.documentId(), status.fileName(), status.chunks(), status.startedAt(), status.finishedAt(),
                elapsedSeconds, estimatedCompletionAt);
    }

    private void index(String jobId, byte[] fileContent, String fileName) {
        long startedAt = System.currentTimeMillis();
        jobs.put(jobId, new JobStatus(jobId, "indexing", 0, "Zpracovávám PDF…", null, fileName, 0,
                startedAt, null, 0, null));
        try {
            DocumentService.UploadResult result = documentService.ingest(fileContent, fileName,
                    progress -> jobs.put(jobId,
                            new JobStatus(jobId, "indexing", progress, "Indexuji…", null, fileName, 0,
                                    startedAt, null, 0, null)));
            long finishedAt = System.currentTimeMillis();
            jobs.put(jobId, new JobStatus(jobId, "complete", 100, "Indexace dokončena.",
                    result.documentId(), result.fileName(), result.chunks(), startedAt, finishedAt, 0, finishedAt));
        } catch (Exception exception) {
            String message = exception.getMessage() == null ? "Indexace selhala." : exception.getMessage();
            jobs.put(jobId, new JobStatus(jobId, "error", 0, message, null, fileName, 0,
                    startedAt, System.currentTimeMillis(), 0, null));
        }
    }

    public record JobStatus(
            String jobId,
            String state,
            int progress,
            String message,
            String documentId,
            String fileName,
            int chunks,
            Long startedAt,
            Long finishedAt,
            long elapsedSeconds,
            Long estimatedCompletionAt) {

    }
}
