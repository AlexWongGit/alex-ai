package org.alex.common.config;

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
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String bucketName;

    private String publicUrl;

    private String endpoint;

    private String accessKey;

    private String secretKey;

}
