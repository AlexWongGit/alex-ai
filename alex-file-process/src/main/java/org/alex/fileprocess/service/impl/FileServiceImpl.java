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

    private static final Map<FileTypeEnum, FileParser> PARSER_MAP = new EnumMap<>(FileTypeEnum.class);

    static {
        PARSER_MAP.put(FileTypeEnum.PDF, new PDFParser());
        PARSER_MAP.put(FileTypeEnum.DOC, new DOCParser());
        PARSER_MAP.put(FileTypeEnum.DOCX, new DOCParser());
        PARSER_MAP.put(FileTypeEnum.XLS, new ExcelParser());
        PARSER_MAP.put(FileTypeEnum.XLSX, new ExcelParser());
        PARSER_MAP.put(FileTypeEnum.PPT, new PPTParser());
        PARSER_MAP.put(FileTypeEnum.PPTX, new PPTParser());
        PARSER_MAP.put(FileTypeEnum.TXT, new TXTParser());
        PARSER_MAP.put(FileTypeEnum.MD, new MDParser());
        PARSER_MAP.put(FileTypeEnum.CSV, new CSVParser());
        PARSER_MAP.put(FileTypeEnum.XML, new XMLParser());
    }

    @Override
    public List<String> splitFile(File file, FileTypeEnum fileType) {
        FileParser parser = PARSER_MAP.get(fileType);

        if (parser == null) {
            throw new UnsupportedOperationException("不支持的文件类型: " + fileType);
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
