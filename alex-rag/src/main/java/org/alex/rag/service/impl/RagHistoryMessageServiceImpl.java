package org.alex.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.alex.common.bean.entity.history.RagHistoryMessage;
import org.alex.rag.mapper.RagHistoryMessageMapper;
import org.alex.rag.service.RagHistoryMessageService;
import org.springframework.stereotype.Service;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/10
 */
@Service
public class RagHistoryMessageServiceImpl extends ServiceImpl<RagHistoryMessageMapper, RagHistoryMessage> implements RagHistoryMessageService {
}
