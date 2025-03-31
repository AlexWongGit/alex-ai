package org.alex.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.alex.common.bean.entity.file.RagFileImages;
import org.alex.rag.mapper.RagFileImagesMapper;
import org.alex.rag.service.RagFileImagesService;
import org.springframework.stereotype.Service;

/**
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
@Service
@RequiredArgsConstructor
public class RagFileImagesServiceImpl extends ServiceImpl<RagFileImagesMapper, RagFileImages> implements RagFileImagesService {
}
