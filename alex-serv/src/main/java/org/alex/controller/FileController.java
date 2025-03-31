package org.alex.controller;

import lombok.extern.slf4j.Slf4j;
import org.alex.common.utils.FileUtil;
import org.alex.fileprocess.service.FileService;
import org.alex.service.AlexServService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: 文件上传控制类
 * @Author wangzf
 * @Date 2025/3/13
 */
@RestController
@RequestMapping("file")
public class FileController {

    @Autowired
    private AlexServService alexServService;

    @RequestMapping("upload")
    public String uploadFileAndSaveToMilvus(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        return alexServService.uploadFileAndSaveToMilvus(file) ? "上传成功" : "上传失败";
    }

    @RequestMapping("batchUpload")
    public Map<String, Boolean> batchUploadFilesAndSaveToMilvus(@RequestPart("files") MultipartFile[] files) {
        Map<String, File> fileMap = new HashMap<>();
        List<File> tempFiles = new CopyOnWriteArrayList<>();
        try {
            for (int i = 0; i < files.length; i++) {
                try {
                    File tempFile = FileUtil.convert(files[i]);
                    fileMap.put(files[i].getOriginalFilename(), tempFile);
                    tempFiles.add(tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return alexServService.batchUploadFileAndSaveToMilvus(fileMap);
        } finally {
            if (!tempFiles.isEmpty()) {
                tempFiles.forEach(File::delete);
            }
        }
    }
}
