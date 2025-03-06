package com.alex.docprocess.service.impl;

import com.alex.docprocess.service.FileService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

    @Override
    public String[] splitFile(File file) {
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
        return fullText.split("\\n\\s*\\n");
    }

    @Override
    public Map<String, String[]> batchUploadFiles(Map<String, File> fileMap) {
        Map<String, String[]> ret = new HashMap<>(1);
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            String fileName = entry.getKey();
            File file = entry.getValue();
            String[] splitFiles = splitFile(file);
            ret.put(fileName, splitFiles);
        }
        return ret;
    }
}
