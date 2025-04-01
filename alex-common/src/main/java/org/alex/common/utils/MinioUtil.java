package org.alex.common.utils;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.alex.common.config.MinioProperties;
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
public class MinioUtil {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;
    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) {
        try {
            String cleanFileName = cleanFileName(file.getOriginalFilename());

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(cleanFileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            return getFileUrl(cleanFileName);
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    public String uploadFile(File file, String fileName) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            String cleanFileName = cleanFileName(fileName);
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(cleanFileName)
                    .stream(fileInputStream, file.length(), -1)
                    .contentType("application/octet-stream")
                    .build()
            );
            return getFileUrl(cleanFileName);
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
                    .bucket(minioProperties.getBucketName())
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
                    .bucket(minioProperties.getBucketName())
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
    private String getFileUrl(String objectName) {
        return String.format("%s/%s/%s", minioProperties.getPublicUrl(), minioProperties.getBucketName(), objectName);
    }

    private static String cleanFileName(String fileName) {

        // 获取文件扩展名
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = fileName.substring(dotIndex);
        }

        // 生成 UUID 并拼接扩展名
        return UUID.randomUUID() + extension;
    }
}
