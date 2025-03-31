package org.alex.rag.module.memory;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.alex.common.bean.entity.history.RagHistoryMessage;
import org.alex.rag.service.RagHistoryMessageService;
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
    private RagHistoryMessageService ragHistoryMessageService;

    public static Message toMessage(RagHistoryMessage aiMessage) {
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

    private static RagHistoryMessage buildHistoryMessage(Message message, String sessionId, UUID messageId, String text, long order) {
        RagHistoryMessage ragHistoryMessage = new RagHistoryMessage();
        ragHistoryMessage.setText(text);
        ragHistoryMessage.setMessageType(getMessageType(message));
        ragHistoryMessage.setMessageId(messageId.toString());
        ragHistoryMessage.setConversationId(sessionId);
        ragHistoryMessage.setMetadata(message.getMetadata().toString());

        Date now = new Date();
        ragHistoryMessage.setCreateTime(now);
        ragHistoryMessage.setAskTime(now);
        ragHistoryMessage.setAnswerTime(now);
        ragHistoryMessage.setMessageOrder(order);

        return ragHistoryMessage;
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

    public static List<RagHistoryMessage> toHistoryMessage(Message message, String sessionId) {
        List<RagHistoryMessage> ragHistoryMessages = new ArrayList<>();
        UUID messageId = UUID.randomUUID();
        List<String> splitTexts = splitString(message.getText(), 4000);

        long order = 0;
        for (String text : splitTexts) {
            RagHistoryMessage ragHistoryMessage = buildHistoryMessage(message, sessionId, messageId, text, order++);
            ragHistoryMessages.add(ragHistoryMessage);
        }

        return ragHistoryMessages;
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
        List<RagHistoryMessage> aiMessages = messages.stream().map(message -> toHistoryMessage(message, conversationId)).flatMap(List::stream).toList();

        ragHistoryMessageService.saveBatch(aiMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        QueryWrapper<RagHistoryMessage> qw = new QueryWrapper<>();
        qw.select("message_id", "MAX(ask_time) AS ask_time")
            .eq("conversation_id", conversationId)
            .groupBy("message_id")
            .orderByDesc("ask_time")
            .last("LIMIT 10");

        List<RagHistoryMessage> uniqueMessages = ragHistoryMessageService.list(qw);
        if (CollUtil.isEmpty(uniqueMessages)) {
            return new ArrayList<>();
        }

        List<String> messageIds = uniqueMessages.stream()
            .map(RagHistoryMessage::getMessageId)
            .toList();

        QueryWrapper<RagHistoryMessage> fullQw = new QueryWrapper<>();
        fullQw.in("message_id", messageIds)
            .orderByAsc("message_order");

        List<RagHistoryMessage> allMessages = ragHistoryMessageService.list(fullQw);

        // 按 messageId 进行分组，并拼接 text 字段
        Map<String, String> mergedTexts = allMessages.stream()
            .collect(Collectors.groupingBy(
                RagHistoryMessage::getMessageId,
                LinkedHashMap::new,
                Collectors.mapping(RagHistoryMessage::getText, Collectors.joining(""))
            ));

        // 遍历所有消息，将拼接后的 text 存入 order = 1 的消息
        List<RagHistoryMessage> resultMessages = new ArrayList<>();
        for (RagHistoryMessage msg : allMessages) {
            if (msg.getMessageOrder() == 0) {
                msg.setText(mergedTexts.get(msg.getMessageId()));
                resultMessages.add(msg);
            }
        }

        return resultMessages.stream().map(HistoryChatMemory::toMessage).toList();
    }

    @Override
    public void clear(String conversationId) {
        ragHistoryMessageService.remove(new LambdaQueryWrapper<RagHistoryMessage>().eq(RagHistoryMessage::getConversationId, conversationId));
    }
}
