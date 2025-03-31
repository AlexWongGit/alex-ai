package org.alex.service.impl;

import org.alex.rag.service.RagService;
import org.alex.service.AlexServService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @Description: AlexServServiceImpl
 * @Author wangzf
 * @Date 2025/3/13
 */
@Service
public class AlexServServiceImpl implements AlexServService {

    private final RagService ragService;

    public AlexServServiceImpl(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException {
       return ragService.uploadFileAndSaveToMilvus(file);
    }

    @Override
    public Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, File> files) {
        return ragService.batchUploadFileAndSaveToMilvus(files);
    }
}
