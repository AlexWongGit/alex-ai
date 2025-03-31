package org.alex.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(minioEndpoint)
            .credentials(minioAccessKey, minioSecretKey)
            .build();
    }

}
