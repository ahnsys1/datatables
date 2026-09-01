package cz.example.airag.document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIndexJobService jobService;
    private final OriginalDocumentStore originalDocumentStore;

    public DocumentController(DocumentIndexJobService jobService, OriginalDocumentStore originalDocumentStore) {
        this.jobService = jobService;
        this.originalDocumentStore = originalDocumentStore;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadStarted> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String jobId = jobService.start(file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.accepted().body(new UploadStarted(jobId));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<DocumentIndexJobService.JobStatus> status(@PathVariable String jobId) {
        DocumentIndexJobService.JobStatus status = jobService.getStatus(jobId);
        return status == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(status);
    }

    @GetMapping(value = "/{documentId}/content", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> content(@PathVariable String documentId) {
        return originalDocumentStore.find(documentId)
                .map(document -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(document.fileName(), StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(document.content().length)
                .body(document.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    record UploadStarted(String jobId) {

    }
}
