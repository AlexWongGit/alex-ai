package org.alex.model.config;

import io.micrometer.observation.ObservationRegistry;
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

/**
 * TODO 功能描述,换行请使用标签<br>
 *
 * @Author xxx （自己的名）
 * @Date 2025/2/10
 */
@Configuration
public class OllamaConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String modelName;

    @Value("${spring.ai.ollama.chat.options.temperature}")
    private Double temperature;



    @Bean
    public OllamaChatModel chatModel()
    {
        var ollamaApi = new OllamaApi(baseUrl);

        return new OllamaChatModel(ollamaApi,
                OllamaOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .build(),
                ToolCallingManager.builder().build(),
                ObservationRegistry.NOOP,
                ModelManagementOptions.defaults());
    }

    @Bean
    public OllamaEmbeddingModel ollamaClient() {
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
