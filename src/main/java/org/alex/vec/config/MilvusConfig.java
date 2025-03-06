package org.alex.vec.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description Milvus配置
 * @author wangzf
 * @date 2025/2/27
 */
@Configuration
public class MilvusConfig {


    @Bean
    public MilvusClientV2 milvusServiceClient() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://127.0.0.1:19530")
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
