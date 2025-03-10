package org.alex.rag.module.memory;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.alex.common.bean.entity.history.HistoryMessage;
import org.alex.service.HistoryMessageService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @Author wangzf
 * @Date 2025/3/10
 */
@Service
public class HistoryChatMemory implements ChatMemory {

    @Autowired
    private HistoryMessageService historyMessageService;

    public static Message toMessage(HistoryMessage aiMessage) {
        if (aiMessage.getMessageType().equals(MessageType.ASSISTANT)) {
            return new AssistantMessage(aiMessage.getAnswer());
        }
        if (aiMessage.getMessageType().equals(MessageType.USER)) {
            return new UserMessage(aiMessage.getAnswer());
        }
        if (aiMessage.getMessageType().equals(MessageType.SYSTEM)) {
            return new SystemMessage(aiMessage.getAnswer());
        }
        throw new RuntimeException("不支持的消息类型");
    }

    public static HistoryMessage toHistoryMessage(Message message, String sessionId) {
        HistoryMessage historyMessage = new HistoryMessage();
        if (message instanceof AssistantMessage assistantMessage) {
            historyMessage.setConversationId(sessionId);
            historyMessage.setMessageType(MessageType.ASSISTANT);
            historyMessage.setAnswer(assistantMessage.getText());
            historyMessage.setMetadata(assistantMessage.getMetadata().toString());
        } else if (message instanceof UserMessage userMessage) {
            historyMessage.setConversationId(sessionId);
            historyMessage.setMessageType(MessageType.USER);
            historyMessage.setAnswer(userMessage.getText());
            historyMessage.setMetadata(userMessage.getMetadata().toString());
        }else if (message instanceof SystemMessage systemMessage) {
            historyMessage.setConversationId(sessionId);
            historyMessage.setMessageType(MessageType.SYSTEM);
            historyMessage.setAnswer(systemMessage.getText());
            historyMessage.setMetadata(systemMessage.getMetadata().toString());
        }  else {
            throw new RuntimeException("不支持的消息类型");
        }

        historyMessage.setCreateTime(new Date());
        historyMessage.setAskTime(new Date());
        historyMessage.setAnswerTime(new Date());
        return historyMessage;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        List<HistoryMessage> aiMessages = messages.stream().map(message -> toHistoryMessage(message, conversationId)).toList();
        historyMessageService.saveBatch(aiMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        LambdaQueryWrapper<HistoryMessage> qw = new LambdaQueryWrapper<>();
        qw.eq(HistoryMessage::getConversationId, conversationId)
            .orderByDesc(HistoryMessage::getAskTime)
            .last("limit " + lastN);
        List<HistoryMessage> historyMessages = historyMessageService.list(qw);
        if (CollUtil.isEmpty(historyMessages)) {
            return new ArrayList<>();
        }
        return historyMessages.stream().map(HistoryChatMemory::toMessage).toList();
    }

    @Override
    public void clear(String conversationId) {
        historyMessageService.remove(new LambdaQueryWrapper<HistoryMessage>().eq(HistoryMessage::getConversationId, conversationId));
    }
}
