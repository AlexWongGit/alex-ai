package org.alex.service.impl;

import io.milvus.grpc.SearchResultData;
import org.alex.entity.SearchSimilarityDto;
import org.alex.service.MilvusService;
import org.alex.service.RagService;
import org.alex.uitls.MilvusUtil;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
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

        // 步骤 2: 在 Milvus 中搜索最相似的文档
        String searchResult = milvusService.searchSimilarity(embedding, null, question);

        // 步骤 3: 将搜索结果和问题一起发送给推理模型
        String context = searchResult != null? searchResult : "";
        String systemPrompt = "你需要根据提供的上下文准确回答用户的问题。";
        String userPrompt = "问题: " + question + "\n上下文: " + context;

        List<Message> messages = Arrays.asList(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        );
        Prompt prompt = new Prompt(messages);

        ChatResponse call = ollamaChatClient.call(prompt);
        return call.getResult().toString();
        // milvus index 向量
    }

}