package org.alex.rag.controller;

import org.alex.common.utils.MilvusUtil;
import lombok.extern.slf4j.Slf4j;
import org.alex.rag.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
@RestController
@RequestMapping("rag")
@Slf4j
public class RAGController {

    @Autowired
    private RagService ragService;

    @RequestMapping("upload")
    public String uploadFileAndSaveToMilvus(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "上传文件为空";
        }
        return ragService.uploadFileAndSaveToMilvus(file) ? "上传成功" : "上传失败";
    }

    @RequestMapping("batchUpload")
    public Map<String, Boolean> batchUploadFilesAndSaveToMilvus(@RequestPart("files") MultipartFile[] files) {
        Map<String, File> fileMap = new HashMap<>();
        List<File> tempFiles = new CopyOnWriteArrayList<>();
        try {
            for (int i = 0; i < files.length; i++) {
                try {
                    File tempFile = MilvusUtil.convert(files[i]);
                    fileMap.put(files[i].getOriginalFilename(), tempFile);
                    tempFiles.add(tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return ragService.batchUploadFileAndSaveToMilvus(fileMap);
        } finally {
            if (!tempFiles.isEmpty()) {
                tempFiles.forEach(File::delete);
            }
        }
    }

    @RequestMapping("ask")
    public String searchTallestSimilarity(@RequestParam("question") String question) {
        return ragService.performRag(question);
    }

}
