package com.alex.docprocess.service;

import java.io.File;
import java.util.Map;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public interface FileService {

    String[] splitFile(File file);

    Map<String, String[]> batchUploadFiles(Map<String, File> fileMap );
}
