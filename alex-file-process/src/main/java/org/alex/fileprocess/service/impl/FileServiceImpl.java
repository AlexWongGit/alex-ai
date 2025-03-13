package org.alex.fileprocess.service.impl;

import org.alex.common.enums.FileTypeEnum;
import org.alex.fileprocess.parser.*;
import org.alex.fileprocess.service.FileService;
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
        } else if (FileTypeEnum.DOC == fileType || FileTypeEnum.DOCX == fileType)
        {
            parser = new DOCParser();
        } else if (FileTypeEnum.XLSX == fileType || FileTypeEnum.XLS == fileType)
        {
            parser = new ExcelParser();
        } else if (FileTypeEnum.PPT == fileType || FileTypeEnum.PPTX == fileType) {
            parser = new PPTParser();
        } else {
            throw new RuntimeException("不支持的文件类型");
        }
        return parser.split2Chunks(file, 1000, fileType);
    }


    @Override
    public Map<String, List<String>> batchSplitFiles(Map<String, File> fileMap) {
        Map<String, List<String>> ret = new HashMap<>(1);
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            try {
                String fileName = entry.getKey();
                File file = entry.getValue();
                List<String> splitFiles = splitFile(file, FileTypeEnum.getFileType(fileName));
                ret.put(fileName, splitFiles);
            }
            catch (Exception e)
            {
                ret.put(entry.getKey(), null);
                e.printStackTrace();
            }
        }
        return ret;
    }
}
