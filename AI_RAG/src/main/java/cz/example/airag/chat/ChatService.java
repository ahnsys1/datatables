package cz.example.airag.chat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import cz.example.airag.config.OllamaModelManager;

@Service
public class ChatService {

    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final String SYSTEM_PROMPT = """
            Jsi cesky asistent pro otazky nad nahranymi dokumenty.
            Odpovidej vzdy cesky, vecne a srozumitelne.
            Pro fakta z dokumentu pouzivej pouze dodany kontext.
            Pokud kontext odpoved neobsahuje, otevrene rekni, ze odpoved v nahranych dokumentech nebyla nalezena.

            Predchozi konverzace:
            %s

            Kontext z dokumentu:
            %s
            """;

    private final OllamaChatGateway chatGateway;
    private final VectorStore vectorStore;
    private final OllamaModelManager ollamaModelManager;
    private final Map<String, Deque<HistoryMessage>> conversations = new ConcurrentHashMap<>();

    public ChatService(OllamaChatGateway chatGateway, VectorStore vectorStore,
            OllamaModelManager ollamaModelManager) {
        this.chatGateway = chatGateway;
        this.vectorStore = vectorStore;
        this.ollamaModelManager = ollamaModelManager;
    }

    public ChatResponse ask(ChatRequest request) {
        List<Document> matches;
        synchronized (ollamaModelManager) {
            try {
                matches = vectorStore.similaritySearch(SearchRequest.builder()
                        .query(request.message())
                        .topK(3)
                        .similarityThreshold(0.55)
                        .build());
            } finally {
                ollamaModelManager.unloadEmbeddingModel();
            }
        }

        Deque<HistoryMessage> history = conversations.computeIfAbsent(
                request.conversationId(), ignored -> new ArrayDeque<>());
        String answer;
        synchronized (history) {
            String prompt = SYSTEM_PROMPT.formatted(formatHistory(history), formatContext(matches));
            answer = chatGateway.chat(prompt, request.message());
            appendHistory(history, new HistoryMessage("Uzivatel", request.message()));
            appendHistory(history, new HistoryMessage("Asistent", answer));
        }

        List<DocumentSource> sources = matches.stream()
                .map(document -> new DocumentSource(
                String.valueOf(document.getMetadata().getOrDefault("documentId", "")),
                String.valueOf(document.getMetadata().getOrDefault("fileName", "Neznamy dokument"))))
                .distinct()
                .toList();
        return new ChatResponse(answer, sources);
    }

    public void clear(String conversationId) {
        conversations.remove(conversationId);
    }

    private String formatHistory(Deque<HistoryMessage> history) {
        if (history.isEmpty()) {
            return "Zatim zadna.";
        }
        return history.stream()
                .map(message -> message.role() + ": " + message.content())
                .collect(Collectors.joining("\n"));
    }

    private String formatContext(List<Document> matches) {
        if (matches.isEmpty()) {
            return "Nebyl nalezen zadny relevantni text.";
        }
        return matches.stream()
                .map(document -> "[" + document.getMetadata().getOrDefault("fileName", "dokument") + "]\n"
                + document.getText())
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private void appendHistory(Deque<HistoryMessage> history, HistoryMessage message) {
        history.addLast(message);
        while (history.size() > MAX_HISTORY_MESSAGES) {
            history.removeFirst();
        }
    }

    private record HistoryMessage(String role, String content) {

    }
}
