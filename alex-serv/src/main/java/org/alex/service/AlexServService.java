package org.alex.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
public interface AlexServService {

    Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException;


    Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, File> files);
}
