package org.alex.rag.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "model")
public class ModelProperties {

    private String deepSeek;

    private String qwen;

    private String llava;

    private Double deepSeekTemperature;

    private Double qwenTemperature;

    private Double llavaTemperature;
}
