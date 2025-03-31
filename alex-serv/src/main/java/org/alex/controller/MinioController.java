package org.alex.controller;

import lombok.RequiredArgsConstructor;
import org.alex.common.utils.MinioUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @description minio文件上传下载
 * @author wangzf
 * @date 2025/3/31
 */
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioUtil minioUtil;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return minioUtil.uploadFile(file);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileName") String fileName) {
        try (InputStream stream = minioUtil.downloadFile(fileName)) {
            byte[] bytes = stream.readAllBytes();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public String deleteFile(@RequestParam("fileName") String fileName) {
        minioUtil.deleteFile(fileName);
        return "删除成功: " + fileName;
    }
}

