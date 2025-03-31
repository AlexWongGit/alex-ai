package org.alex.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.alex.common.bean.entity.history.RagHistoryMessage;
import org.alex.rag.mapper.HistoryMessageMapper;
import org.alex.rag.service.HistoryMessageService;
import org.springframework.stereotype.Service;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/10
 */
@Service
public class HistoryMessageServiceImpl extends ServiceImpl<HistoryMessageMapper, RagHistoryMessage> implements HistoryMessageService {
}
