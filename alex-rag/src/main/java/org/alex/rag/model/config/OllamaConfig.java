package org.alex.rag.model.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 *
 * @Author wangzf
 * @Date 2025/2/10
 */
@Configuration
@RefreshScope
public class OllamaConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String deepSeekModelName;

    @Value("${qwen.model}")
    private String qwenModelName;

    @Value("${deepseek.temperature}")
    private Double deepSeekTemperature;

    @Value("${qwen.temperature}")
    private Double qwenTemperature;





    @Bean(name = "deepSeekClient")
    @Primary
    public OllamaChatModel deepSeekClient()
    {
        var ollamaApi = new OllamaApi(baseUrl);

        return new OllamaChatModel(ollamaApi,
                OllamaOptions.builder()
                        .model(deepSeekModelName)
                        .temperature(deepSeekTemperature)
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
                        .model(qwenModelName)
                        .temperature(qwenTemperature)
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
                        .model(OllamaModel.NOMIC_EMBED_TEXT)
                        .build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults()
                );
    }
}
