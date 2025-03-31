package org.alex.common.utils;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * minio工具类
 * @Author wangzf
 * @Date 2025/3/31
 */
@Component
@RequiredArgsConstructor
@RefreshScope
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-url}")
    private String publicUrl;

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) {
        try {
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            return getFileUrl(objectName);
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    public String uploadFile(File file, String fileName) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(fileInputStream, file.length(), -1)
                    .contentType("application/octet-stream")
                    .build()
            );
            return getFileUrl(fileName);
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("下载失败", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("删除失败", e);
        }
    }

    /**
     * 生成文件的访问 URL
     */
    public String getFileUrl(String objectName) {
        return String.format("%s/%s/%s", publicUrl, bucketName, objectName);
    }
}
