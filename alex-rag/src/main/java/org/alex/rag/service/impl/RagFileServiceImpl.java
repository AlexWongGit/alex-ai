package org.alex.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.alex.common.bean.entity.file.RAGFile;
import org.alex.common.enums.FileTypeEnum;
import org.alex.common.utils.MinioUtil;
import org.alex.rag.mapper.RagFileMapper;
import org.alex.rag.service.RagFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
@Service
@RequiredArgsConstructor
public class RagFileServiceImpl extends ServiceImpl<RagFileMapper, RAGFile> implements RagFileService {
    private final MinioUtil minioUtil;

    @Override
    public boolean durationFile(MultipartFile file, FileTypeEnum fileType) {
        String fileUrl = null;
        try {
            fileUrl = minioUtil.uploadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RAGFile ragFile = new RAGFile();
        ragFile.setFileUrl(fileUrl);
        ragFile.setFileType(fileType.getType());
        ragFile.setFileName(file.getOriginalFilename());
        return this.save(ragFile);
    }
}
