package org.alex.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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
}
