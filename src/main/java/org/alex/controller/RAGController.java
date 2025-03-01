package org.alex.controller;

import lombok.extern.slf4j.Slf4j;
import org.alex.service.FileService;
import org.alex.service.MilvusService;
import org.alex.service.RagService;
import org.alex.uitls.MilvusUtil;
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
    private MilvusService milvusService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RagService ragService;

    @RequestMapping("upload")
    public String uploadFileAndSaveToMilvus(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "上传文件为空";
        }
        File tempFile = MilvusUtil.convert(file);
        try {
            // 使用转换后的 File 对象进行后续操作
            log.info("转换后的文件路径: {}" + tempFile.getAbsolutePath());
            return fileService.uploadFileAndSaveToMilvus(file.getName(), tempFile) ? "上传成功" : "上传失败";
        } finally {
            // 删除临时文件
            tempFile.delete();
        }
    }

    @RequestMapping("batchUpload")
    public String batchUploadFilesAndSaveToMilvus(@RequestPart("files") MultipartFile[] files) {
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
            return fileService.batchUploadFilesAndSaveToMilvus(fileMap).toString();
        }finally {
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
