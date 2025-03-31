package org.alex.rag.service.impl;

import jakarta.annotation.Resource;
import org.alex.common.enums.FileTypeEnum;
import org.alex.common.utils.FileUtil;
import org.alex.fileprocess.service.FileProcessService;
import org.alex.common.bean.dto.ArchiveDto;
import org.alex.rag.module.memory.HistoryChatMemory;
import org.alex.rag.module.prompt.PromptTemplateConstants;
import org.alex.rag.service.RagFileService;
import org.alex.rag.service.RagService;
import org.alex.vec.service.MilvusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author wangzf
 * @date 2025/2/27
 */
@Service
@Slf4j
public class RagServiceImpl implements RagService {

    @Resource
    @Qualifier("bgeM3EmbeddingClient")
    private OllamaEmbeddingModel ollamaEmbeddingModel;

    @Resource
    private OllamaChatModel deepSeekClient;

    @Resource
    @Qualifier("qWenClient")
    private OllamaChatModel qWenClient;

    @Resource
    private MilvusService milvusService;

    @Resource
    private FileProcessService fileProcessService;

    @Resource
    private RagFileService ragFileService;

    @Resource
    private HistoryChatMemory chatMemory;

    @Override
    public Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException {
        File tempFile = FileUtil.convert(file);
        try {
            // 使用转换后的 File 对象进行后续操作
            CompletableFuture<Void> durationTask =
                CompletableFuture.runAsync(() -> ragFileService.durationFile(file, FileTypeEnum.getFileType(file.getOriginalFilename())));
            CompletableFuture<Void> vectorTask = CompletableFuture.runAsync(() -> storageVector(file, tempFile));
            CompletableFuture.allOf(durationTask, vectorTask).join();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 删除临时文件
            tempFile.delete();
        }
        return true;
    }

    private void storageVector(MultipartFile file, File tempFile) {
        List<String> textArray = fileProcessService.splitFile(tempFile, FileTypeEnum.getFileType(file.getOriginalFilename()));
        log.info("转换后的文件路径: {}" + tempFile.getAbsolutePath());
        for (String s : textArray) {
            save2Milvus(file.getOriginalFilename(), s);
        }
    }

    private boolean save2Milvus(String fileName, String s) {
        ArchiveDto archive = new ArchiveDto();
        // 生成嵌入向量
        float[] embedding = ollamaEmbeddingModel.embed(s);
        archive.setFileName(fileName);
        archive.setArcsoftFeature(embedding);
        archive.setOrgId(1);
        archive.setText(s);
        return milvusService.insert(Collections.singletonList(archive));
    }

    @Override
    public Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, File> files) {
        Map<String, Boolean> ret = new HashMap<>(1);

        Map<String, List<String>> map = fileProcessService.batchSplitFiles(files);
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String fileName = entry.getKey();
            List<String> textArray = entry.getValue();
            if (textArray == null) {
                ret.put(fileName, false);
                continue;
            }
            for (String s : textArray) {
                try {
                    ret.put(fileName, save2Milvus(fileName, s));
                } catch (Exception e) {
                    ret.put(fileName, false);
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    @Override
    public String performRag(String question) {

        // 步骤 1: 使用大模型提取关键词生成向量
        PromptTemplate promptTemplate = new PromptTemplate(PromptTemplateConstants.GET_KEYWORDS_TEMPLATE);
        Prompt prompt1 = promptTemplate.create(Map.of("question", question));
        ChatResponse chatResponse = qWenClient.call(prompt1);
        String keyword = extractKeywords(chatResponse.getResult().getOutput().getText());
        if (keyword.isEmpty()) {
            keyword = question;
        }
        log.info("关键词：{}", keyword);

        // 步骤 2: 生成关键字的嵌入向量
        float[] embedding = ollamaEmbeddingModel.embed(keyword);

        // 步骤 3: 数据检索阶段, 在 Milvus 中搜索最相似的文档
        String searchResult = milvusService.searchSimilarity(embedding, null, keyword);

        // 步骤 4: 信息增强与整合
        String context = searchResult != null ? searchResult : "";

        // 步骤 5: 将搜索结果和问题一起发送给推理模型
        String systemPrompt = "你需要根据提供的上下文准确回答用户的问题。";
        PromptTemplate promptTemplate1 = new PromptTemplate(PromptTemplateConstants.PROMPT_RAG);
        Prompt userPrompt = promptTemplate1.create(Map.of("query", question, "context", context));
        // 步骤 6: 自然语言生成, 获取模型生成的答案
        ChatResponse response = ChatClient.create(deepSeekClient)
            .prompt(userPrompt)
            .system(systemPrompt)
            //.tools(functionBeanNames)
            // 历史问答
            .advisors(advisorSpec -> fillHistory(advisorSpec, "12345"))
            .call()
            .chatResponse();
        return response.getResult().toString();
    }

    public void fillHistory(ChatClient.AdvisorSpec advisorSpec, String sessionId) {
        // 1. 如果需要存储会话和消息到数据库，自己可以实现ChatMemory接口，这里使用自己实现的AiMessageChatMemory，数据库存储。
        // 2. 传入会话id，MessageChatMemoryAdvisor会根据会话id去查找消息。
        // 3. 只需要携带最近10条消息
        // MessageChatMemoryAdvisor会在消息发送给大模型之前，从ChatMemory中获取会话的历史消息，然后一起发送给大模型。
        advisorSpec.advisors(new MessageChatMemoryAdvisor(chatMemory, sessionId, 10));
    }

    public static String extractKeywords(String input) {
        int startIndex = input.lastIndexOf("关键词：");
        if (startIndex == -1) {
            return "";
        }
        startIndex += "关键词：".length();
        int endIndex = input.lastIndexOf("、回答完毕");
        if (endIndex == -1) {
            endIndex = input.length();
        }
        if (startIndex >= endIndex) {
            return "";
        }
        return input.substring(startIndex, endIndex).trim();
    }

}