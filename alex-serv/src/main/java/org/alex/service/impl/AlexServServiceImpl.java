package org.alex.service.impl;

import org.alex.fileprocess.service.impl.FileProcessServiceImpl;
import org.alex.rag.service.RagService;
import org.alex.service.AlexServService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: AlexServServiceImpl
 * @Author wangzf
 * @Date 2025/3/13
 */
@Service
public class AlexServServiceImpl implements AlexServService {
    private static final Logger logger = LoggerFactory.getLogger(AlexServServiceImpl.class);

    private final RagService ragService;

    public AlexServServiceImpl(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException {
       return ragService.uploadFileAndSaveToMilvus(file);
    }

    @Override
    public Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, MultipartFile> files) {
        Map<String, Boolean> ret = new HashMap<>(1);

        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            try {
                String fileName = entry.getKey();
                MultipartFile file = entry.getValue();
                ret.put(fileName, ragService.uploadFileAndSaveToMilvus(file));
            } catch (Exception e) {
                ret.put(entry.getKey(), false);
                logger.error("Error upload file: " + entry.getKey(), e);
            }
        }
        return ret;
    }
}
