package org.alex.service.impl;

import org.alex.entity.ArchiveDto;
import org.alex.service.FileService;
import org.alex.uitls.MilvusUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Author wangzf
 * @Date 2025/2/27
 */
@Service
public class FileServiceImpl implements FileService {

    private final MilvusServiceImpl milvusService;

    private final OllamaEmbeddingModel embeddingModel;


    public FileServiceImpl(MilvusServiceImpl milvusService, OllamaEmbeddingModel embeddingModel) {
        this.milvusService = milvusService;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public Boolean uploadFileAndSaveToMilvus(String fileName, File file) {
        ArchiveDto archive = new ArchiveDto();
        byte[] fileBytes = processFile(file);
        archive.setArchiveId(1L);
        archive.setArcsoftFeature(fileBytes);
        archive.setOrgId(1);
        return milvusService.insert(Collections.singletonList(archive));
    }

    @Override
    public Map<String, Boolean> batchUploadFilesAndSaveToMilvus(Map<String, File> fileMap) {
        Map<String, Boolean> ret = new HashMap<>(1);
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            String fileName = entry.getKey();
            File file = entry.getValue();
            Boolean isSuccess = uploadFileAndSaveToMilvus(fileName, file);
            ret.put(fileName, isSuccess);
        }
        return ret;
    }

    private byte[] processFile(File file) {
        // 从文件中提取文本
        StringBuilder content = new StringBuilder();
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            content.append(stripper.getText(document));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 生成嵌入向量
        float[] embedding = embeddingModel.embed(content.toString());

        return MilvusUtil.convertEmbeddingsToBytes(Collections.singletonList(embedding));
    }
}
