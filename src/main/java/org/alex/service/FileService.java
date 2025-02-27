package org.alex.service;

import java.io.File;
import java.util.Map;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public interface FileService {

    Boolean uploadFileAndSaveToMilvus(String fileName, File file);

    Map<String, Boolean> batchUploadFilesAndSaveToMilvus(Map<String, File> fileMap );
}
