package org.alex.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.alex.common.bean.entity.file.RAGFile;
import org.alex.common.enums.FileTypeEnum;

import java.io.File;

/**
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
public interface RagFileService extends IService<RAGFile> {

    boolean durationFile(File file, FileTypeEnum fileType,String fileName);
}
