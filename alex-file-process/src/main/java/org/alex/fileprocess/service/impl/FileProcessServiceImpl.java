package org.alex.fileprocess.service.impl;

import org.alex.common.bean.entity.file.RAGFile;
import org.alex.common.enums.FileTypeEnum;
import org.alex.common.utils.MinioUtil;
import org.alex.fileprocess.parser.*;
import org.alex.fileprocess.service.FileProcessService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * @Author wangzf
 * @Date 2025/2/27
 */
@Service
public class FileProcessServiceImpl implements FileProcessService {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessServiceImpl.class);
    private static final Map<FileTypeEnum, FileParser> PARSER_MAP = initializeParserMap();

    private static Map<FileTypeEnum, FileParser> initializeParserMap() {
        Map<FileTypeEnum, FileParser> parserMap = new EnumMap<>(FileTypeEnum.class);
        parserMap.put(FileTypeEnum.PDF, new PDFParser());
        parserMap.put(FileTypeEnum.DOC, new DOCParser());
        parserMap.put(FileTypeEnum.DOCX, new DOCParser());
        parserMap.put(FileTypeEnum.XLS, new ExcelParser());
        parserMap.put(FileTypeEnum.XLSX, new ExcelParser());
        parserMap.put(FileTypeEnum.PPT, new PPTParser());
        parserMap.put(FileTypeEnum.PPTX, new PPTParser());
        parserMap.put(FileTypeEnum.TXT, new TXTParser());
        parserMap.put(FileTypeEnum.MD, new MDParser());
        parserMap.put(FileTypeEnum.CSV, new CSVParser());
        parserMap.put(FileTypeEnum.XML, new XMLParser());
        return parserMap;
    }

    @Override
    public List<String> splitFile(File file, FileTypeEnum fileType) {
        return Optional.ofNullable(PARSER_MAP.get(fileType))
            .orElseThrow(() -> new UnsupportedOperationException("不支持的文件类型: " + fileType))
            .split2Chunks(file, 1000, fileType);
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
            } catch (Exception e) {
                ret.put(entry.getKey(), null);
                logger.error("Error processing file: " + entry.getKey(), e);
            }
        }
        return ret;
    }
}