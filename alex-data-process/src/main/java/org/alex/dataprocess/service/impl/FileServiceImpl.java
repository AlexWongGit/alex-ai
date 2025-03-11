package org.alex.dataprocess.service.impl;

import org.alex.common.enums.FileTypeEnum;
import org.alex.dataprocess.processor.FileParser;
import org.alex.dataprocess.processor.PDFParser;
import org.alex.dataprocess.service.FileService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * @Author wangzf
 * @Date 2025/2/27
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public List<String> splitFile(File file, FileTypeEnum fileType) {
        FileParser parser;
        if (FileTypeEnum.PDF == fileType) {
            parser = new PDFParser();
        } else {
            throw new RuntimeException("不支持的文件类型");
        }
        return parser.split2Chunks(file, 1000);
    }


    @Override
    public Map<String, List<String>> batchUploadFiles(Map<String, File> fileMap) {
        Map<String, List<String>> ret = new HashMap<>(1);
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            String fileName = entry.getKey();
            File file = entry.getValue();
            List<String> splitFiles = splitFile(file, FileTypeEnum.getFileType(fileName));
            ret.put(fileName, splitFiles);
        }
        return ret;
    }
}
