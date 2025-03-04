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
        // 从文件中提取文本
        StringBuilder content = new StringBuilder();
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            content.append(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 构造字符串数组，按段落拆分文本
        String fullText = content.toString();
        // 按空行拆分段落
        String[] textArray = fullText.split("\\n\\s*\\n");

        for (int i = 0; i < textArray.length; i++) {
            float[] embedding = embeddingModel.embed(content.toString());
            archive.setFileName(fileName);
            archive.setArcsoftFeature(embedding);
            archive.setOrgId(1);
            archive.setText(textArray[i]);
            milvusService.insert(Collections.singletonList(archive));
        }

        // 生成嵌入向量

        return true;
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
}
