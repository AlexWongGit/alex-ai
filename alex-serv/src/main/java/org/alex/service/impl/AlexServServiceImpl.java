package org.alex.service.impl;

import org.alex.common.bean.dto.ArchiveDto;
import org.alex.common.enums.FileTypeEnum;
import org.alex.common.utils.FileUtil;
import org.alex.fileprocess.service.FileService;
import org.alex.rag.service.RagService;
import org.alex.service.AlexServService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
@Service
public class AlexServServiceImpl implements AlexServService {

    @Autowired
    private RagService ragService;

    @Override
    public Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException {
       return ragService.uploadFileAndSaveToMilvus(file);
    }

    @Override
    public Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, File> files) {
        return ragService.batchUploadFileAndSaveToMilvus(files);
    }
}
