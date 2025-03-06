package com.alex.vec.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description Milvus配置
 * @author wangzf
 * @date 2025/2/27
 */
@Configuration
public class MilvusConfig {

    @Value("${milvus.baseUrl}")
    private String baseUrl;

    @Bean
    public MilvusClientV2 milvusServiceClient() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(baseUrl)
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
