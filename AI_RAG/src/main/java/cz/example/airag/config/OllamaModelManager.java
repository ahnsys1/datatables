package cz.example.airag.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OllamaModelManager {

    private final RestClient ollamaClient;
    private final String embeddingModel;
    private final String chatModel;

    public OllamaModelManager(
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.embedding.options.model}") String embeddingModel,
            @Value("${spring.ai.ollama.chat.options.model}") String chatModel) {
        this.ollamaClient = restClientBuilder.baseUrl(baseUrl).build();
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    public void unloadEmbeddingModel() {
        unload(embeddingModel);
    }

    public void unloadChatModel() {
        unload(chatModel);
    }

    private void unload(String model) {
        ollamaClient.post()
                .uri("/api/generate")
                .body(Map.of("model", model, "keep_alive", 0))
                .retrieve()
                .toBodilessEntity();
    }
}
