package cz.example.airag.chat;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cz.example.airag.config.OllamaModelManager;

@Component
public class OllamaChatGateway {

    private final RestClient ollamaClient;
    private final String chatModel;
    private final OllamaModelManager ollamaModelManager;

    public OllamaChatGateway(
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.chat.options.model}") String chatModel,
            OllamaModelManager ollamaModelManager) {
        this.ollamaClient = restClientBuilder.baseUrl(baseUrl).build();
        this.chatModel = chatModel;
        this.ollamaModelManager = ollamaModelManager;
    }

    public String chat(String systemPrompt, String userMessage) {
        Map<String, Object> request = Map.of(
                "model", chatModel,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)),
                "options", Map.of(
                        "temperature", 0.2,
                        "num_ctx", 2048,
                        "num_predict", 256));

        OllamaChatResponse response;
        synchronized (ollamaModelManager) {
            ollamaModelManager.unloadEmbeddingModel();
            try {
                response = ollamaClient.post()
                        .uri("/api/chat")
                        .body(request)
                        .retrieve()
                        .body(OllamaChatResponse.class);
            } finally {
                ollamaModelManager.unloadChatModel();
            }
        }
        if (response == null || response.message() == null || response.message().content().isBlank()) {
            throw new IllegalStateException("Model vratil prazdnou odpoved.");
        }
        return response.message().content();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaChatResponse(OllamaMessage message) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaMessage(String content) {

    }
}
