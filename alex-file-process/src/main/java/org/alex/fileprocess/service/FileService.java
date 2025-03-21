package org.alex.fileprocess.service;

import org.alex.common.enums.FileTypeEnum;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public interface FileService {

    List<String> splitFile(File file, FileTypeEnum fileType);

    Map<String, List<String>> batchSplitFiles(Map<String, File> fileMap );
}
