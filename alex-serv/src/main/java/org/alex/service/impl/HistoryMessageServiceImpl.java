package org.alex.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.alex.common.bean.entity.history.HistoryMessage;
import org.alex.mapper.HistoryMessageMapper;
import org.alex.service.HistoryMessageService;
import org.springframework.stereotype.Service;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/10
 */
@Service
public class HistoryMessageServiceImpl extends ServiceImpl<HistoryMessageMapper, HistoryMessage> implements HistoryMessageService {
}
