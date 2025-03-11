package org.alex.rag.module.memory;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.alex.common.bean.entity.history.HistoryMessage;
import org.alex.service.HistoryMessageService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
            return new AssistantMessage(aiMessage.getText());
        }
        if (aiMessage.getMessageType().equals(MessageType.USER)) {
            return new UserMessage(aiMessage.getText());
        }
        if (aiMessage.getMessageType().equals(MessageType.SYSTEM)) {
            return new SystemMessage(aiMessage.getText());
        }
        throw new RuntimeException("不支持的消息类型");
    }

    private static HistoryMessage buildHistoryMessage(Message message, String sessionId, UUID messageId, String text, long order) {
        HistoryMessage historyMessage = new HistoryMessage();
        historyMessage.setText(text);
        historyMessage.setMessageType(getMessageType(message));
        historyMessage.setMessageId(messageId.toString());
        historyMessage.setConversationId(sessionId);
        historyMessage.setMetadata(message.getMetadata().toString());

        Date now = new Date();
        historyMessage.setCreateTime(now);
        historyMessage.setAskTime(now);
        historyMessage.setAnswerTime(now);
        historyMessage.setMessageOrder(order);

        return historyMessage;
    }

    private static MessageType getMessageType(Message message) {
        if (message instanceof AssistantMessage) {
            return MessageType.ASSISTANT;
        }
        if (message instanceof UserMessage) {
            return MessageType.USER;
        }
        if (message instanceof SystemMessage) {
            return MessageType.SYSTEM;
        }
        throw new RuntimeException("不支持的消息类型: " + message.getClass().getSimpleName());
    }

    public static List<HistoryMessage> toHistoryMessage(Message message, String sessionId) {
        List<HistoryMessage> historyMessages = new ArrayList<>();
        UUID messageId = UUID.randomUUID();
        List<String> splitTexts = splitString(message.getText(), 4000);

        long order = 0;
        for (String text : splitTexts) {
            HistoryMessage historyMessage = buildHistoryMessage(message, sessionId, messageId, text, order++);
            historyMessages.add(historyMessage);
        }

        return historyMessages;
    }

    public static List<String> splitString(String input, int chunkSize) {
        List<String> result = new ArrayList<>();
        int length = input.length();

        for (int i = 0; i < length; i += chunkSize) {
            result.add(input.substring(i, Math.min(length, i + chunkSize)));
        }

        return result;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        List<HistoryMessage> aiMessages = messages.stream().map(message -> toHistoryMessage(message, conversationId)).flatMap(List::stream).toList();

        historyMessageService.saveBatch(aiMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        QueryWrapper<HistoryMessage> qw = new QueryWrapper<>();
        qw.select("message_id", "MAX(ask_time) AS ask_time")
            .eq("conversation_id", conversationId)
            .groupBy("message_id")
            .orderByDesc("ask_time")
            .last("LIMIT 10");

        List<HistoryMessage> uniqueMessages = historyMessageService.list(qw);
        if (CollUtil.isEmpty(uniqueMessages)) {
            return new ArrayList<>();
        }

        List<String> messageIds = uniqueMessages.stream()
            .map(HistoryMessage::getMessageId)
            .toList();

        QueryWrapper<HistoryMessage> fullQw = new QueryWrapper<>();
        fullQw.in("message_id", messageIds)
            .orderByAsc("message_order");

        List<HistoryMessage> allMessages = historyMessageService.list(fullQw);

        // 按 messageId 进行分组，并拼接 text 字段
        Map<String, String> mergedTexts = allMessages.stream()
            .collect(Collectors.groupingBy(
                HistoryMessage::getMessageId,
                LinkedHashMap::new,
                Collectors.mapping(HistoryMessage::getText, Collectors.joining(""))
            ));

        // 遍历所有消息，将拼接后的 text 存入 order = 1 的消息
        List<HistoryMessage> resultMessages = new ArrayList<>();
        for (HistoryMessage msg : allMessages) {
            if (msg.getMessageOrder() == 0) {
                msg.setText(mergedTexts.get(msg.getMessageId()));
                resultMessages.add(msg);
            }
        }

        return resultMessages.stream().map(HistoryChatMemory::toMessage).toList();
    }

    @Override
    public void clear(String conversationId) {
        historyMessageService.remove(new LambdaQueryWrapper<HistoryMessage>().eq(HistoryMessage::getConversationId, conversationId));
    }
}
