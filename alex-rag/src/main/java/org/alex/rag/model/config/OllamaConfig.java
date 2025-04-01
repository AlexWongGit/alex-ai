package org.alex.rag.model.config;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 *
 * @Author wangzf
 * @Date 2025/2/10
 */
@Configuration
@RequiredArgsConstructor
public class OllamaConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    private final ModelProperties modelProperties;


    @Bean(name = "deepSeekClient")
    @Primary
    public OllamaChatModel deepSeekClient()
    {
        var ollamaApi = new OllamaApi(baseUrl);

        return new OllamaChatModel(ollamaApi,
                OllamaOptions.builder()
                        .model(modelProperties.getDeepSeek())
                        .temperature(modelProperties.getDeepSeekTemperature())
                        .build(),
                ToolCallingManager.builder().build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults());
    }

    @Bean(name = "qWenClient")
    public OllamaChatModel qWenClient()
    {
        var ollamaApi = new OllamaApi(baseUrl);

        return new OllamaChatModel(ollamaApi,
                OllamaOptions.builder()
                        .model(modelProperties.getQwen())
                        .temperature(modelProperties.getQwenTemperature())
                        .build(),
                ToolCallingManager.builder().build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults());
    }

    @Bean(name = "visionClient")
    public OllamaChatModel visionClient()
    {
        var ollamaApi = new OllamaApi(baseUrl);

        return new OllamaChatModel(ollamaApi,
                OllamaOptions.builder()
                        .model(modelProperties.getLlava())
                        .temperature(modelProperties.getLlavaTemperature())
                        .build(),
                ToolCallingManager.builder().build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults());
    }

    @Bean(name = "nomicEmbeddingClient")
    @Primary
    public OllamaEmbeddingModel nomicEmbeddingClient() {
        var ollamaApi = new OllamaApi(baseUrl);
        return new OllamaEmbeddingModel(ollamaApi,
                OllamaOptions.builder()
                        .model(OllamaModel.NOMIC_EMBED_TEXT)
                        .build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults()
                );
    }

    @Bean(name = "bgeM3EmbeddingClient")
    public OllamaEmbeddingModel bgeM3EmbeddingClient() {
        var ollamaApi = new OllamaApi(baseUrl);
        return new OllamaEmbeddingModel(ollamaApi,
                OllamaOptions.builder()
                        .model("bge-m3")
                        .build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults()
                );
    }
}
