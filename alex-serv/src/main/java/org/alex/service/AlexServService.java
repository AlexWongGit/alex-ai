package org.alex.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @Description: 业务接口
 * @Author wangzf
 * @Date 2025/3/13
 */
public interface AlexServService {

    /**
     * 上传文件并保存到milvus
     * @param file 文件
     * @return Boolean
     * @throws IOException io异常
     */
    Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException;


    /**
     * 批量上传文件并保存到milvus
     * @param files 文件
     * @return Map<String, Boolean>
     */
    Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, File> files);
}
