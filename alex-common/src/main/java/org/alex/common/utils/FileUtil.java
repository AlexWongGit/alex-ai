package org.alex.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

/**
 * 文件工具类
 *
 * @Author wangzf
 * @Date 2025/3/12
 */
public class FileUtil {

    public static File convert(MultipartFile multipartFile) throws IOException {
        // 创建一个临时文件
        File tempFile = File.createTempFile("temp", null);
        // 将 MultipartFile 内容传输到临时文件
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    public static String encodeImageToBase64(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream is = url.openStream(); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(os.toByteArray());
        }
    }
}
