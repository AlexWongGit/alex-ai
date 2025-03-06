package org.alex.rag.service.impl;

import org.alex.vec.service.MilvusService;
import org.alex.rag.service.RagService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author wangzf
 * @date 2025/2/27
 */
@Service
public class RagServiceImpl implements RagService {

    private final OllamaEmbeddingModel embeddingClient;
    private final OllamaChatModel ollamaChatClient;

    private final MilvusService milvusService;

    public RagServiceImpl(OllamaEmbeddingModel embeddingClient, OllamaChatModel ollamaChatClient, MilvusService milvusService) {
        this.embeddingClient = embeddingClient;
        this.ollamaChatClient = ollamaChatClient;
        this.milvusService = milvusService;
    }

    @Override
    public String performRag(String question) {
        // 步骤 1: 生成问题的嵌入向量
        float[] embedding = embeddingClient.embed(question);

        // 步骤 2: 数据检索阶段, 在 Milvus 中搜索最相似的文档
        String searchResult = milvusService.searchSimilarity(embedding, null, question);

        // 步骤 3: 信息增强与整合

        // 步骤 4: 将搜索结果和问题一起发送给推理模型
        String context = searchResult != null? searchResult : "";
        String systemPrompt = "你需要根据提供的上下文准确回答用户的问题。";
        String userPrompt = "问题: " + question + "\n上下文: " + context;

        List<Message> messages = Arrays.asList(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        );
        Prompt prompt = new Prompt(messages);


        // 步骤 5: 自然语言生成, 获取模型生成的答案transformer、rnn
        ChatResponse call = ollamaChatClient.call(prompt);

        // 步骤 6: 动态融合与输出 attention

        // 步骤 7: 返回结果
        return call.getResult().toString();
    }

}