package org.alex.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.alex.common.bean.entity.file.RAGFile;
import org.alex.common.enums.FileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
public interface RagFileService extends IService<RAGFile> {

    boolean durationFile(MultipartFile file, FileTypeEnum fileType);
}
