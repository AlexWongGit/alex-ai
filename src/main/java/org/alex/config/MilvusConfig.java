package org.alex.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
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
    public MilvusServiceClient milvusServiceClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost("127.0.0.1")
                .withPort(19530)
                .build();
        return new MilvusServiceClient(connectParam);
    }
}
