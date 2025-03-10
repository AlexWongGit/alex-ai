package org.alex.rag.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.alex.dataprocess.service.FileService;
import org.alex.common.bean.dto.ArchiveDto;
import org.alex.rag.module.memory.HistoryChatMemory;
import org.alex.rag.module.prompt.PromptTemplateConstants;
import org.alex.rag.service.RagService;
import org.alex.common.utils.MilvusUtil;
import org.alex.vec.service.MilvusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author wangzf
 * @date 2025/2/27
 */
@Service
@Slf4j
public class RagServiceImpl implements RagService {

    @Resource
    private OllamaEmbeddingModel embeddingModel;

    @Resource
    @Qualifier("deepSeekClient")
    private OllamaChatModel deepSeekClient;

    @Resource
    @Qualifier("qWenClient")
    private  OllamaChatModel qWenClient;

    @Resource
    private MilvusService milvusService;

    @Resource
    private FileService fileService;

    @Resource
    private HistoryChatMemory chatMemory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException {
        File tempFile = MilvusUtil.convert(file);
        try
        {
            // 使用转换后的 File 对象进行后续操作
            String[] textArray = fileService.splitFile(tempFile);
            log.info("转换后的文件路径: {}" + tempFile.getAbsolutePath());
            for (String s : textArray)
            {
                ArchiveDto archive = new ArchiveDto();
                // 生成嵌入向量
                float[] embedding = embeddingModel.embed(s);
                archive.setFileName(file.getName());
                archive.setArcsoftFeature(embedding);
                archive.setOrgId(1);
                archive.setText(s);
                milvusService.insert(Collections.singletonList(archive));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            // 删除临时文件
            tempFile.delete();
        }
        return true;
    }

    @Override
    public Map<String, Boolean> batchUploadFileAndSaveToMilvus(Map<String, File> files) {
        Map<String, String[]> map = fileService.batchUploadFiles(files);
        Map<String, Boolean> ret = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String fileName = entry.getKey();
            String[] textArray = entry.getValue();
            for (String s : textArray) {
                ArchiveDto archive = new ArchiveDto();
                // 生成嵌入向量
                float[] embedding = embeddingModel.embed(s);
                archive.setFileName(fileName);
                archive.setArcsoftFeature(embedding);
                archive.setOrgId(1);
                archive.setText(s);
                Boolean insert = milvusService.insert(Collections.singletonList(archive));
                ret.put(fileName, insert);
            }
        }
        return ret;
    }

/*    @Override
    public String performRag(String question) {
        // 步骤 1: 生成问题的嵌入向量
        float[] embedding = embeddingModel.embed(question);

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
    */
    @Override
    public Flux<ServerSentEvent<String>> performRag(String question) {
        // 步骤 1: 使用大模型提取关键词生成向量
        PromptTemplate promptTemplate = new PromptTemplate(PromptTemplateConstants.GET_KEYWORDS_TEMPLATE);
        Prompt prompt1 = promptTemplate.create(Map.of("question", question));

        ChatResponse chatResponse = qWenClient.call(prompt1);

        String resp = chatResponse.getResult().toString();
        String keyword = extractKeywords(resp);
        if (keyword.isEmpty()) {
            keyword = question;
        }
        log.info("关键词：{}", keyword);
        // 步骤 2: 生成关键字的嵌入向量
        float[] embedding = embeddingModel.embed(keyword);

        // 步骤 3: 数据检索阶段, 在 Milvus 中搜索最相似的文档
        String searchResult = milvusService.searchSimilarity(embedding, null, keyword);

        // 步骤 4: 信息增强与整合



        // 步骤 5: 将搜索结果和问题一起发送给推理模型
        String systemPrompt = "你需要根据提供的上下文准确回答用户的问题。";

        String context = searchResult != null? searchResult : "";
        String userPrompt = "问题: " + question + "\n上下文: " + context;
        // 步骤 6: 自然语言生成, 获取模型生成的答案transformer、rnn
        //String[] functionBeanNames = new String[0];
        return ChatClient.create(deepSeekClient).prompt()
            .system(systemPrompt)
            .user(userPrompt)
            //.tools(functionBeanNames)
            .advisors(advisorSpec -> {
                fillHistory(advisorSpec, "12345");
                //useVectorStore(advisorSpec, aiMessageWrapper.getParams().getEnableVectorStore());
            }).stream()
            .chatResponse()
            .map(response -> ServerSentEvent.builder(toJson(response))
                // 和前端监听的事件相对应
                .event("message").build());
    }

    @SneakyThrows
    public String toJson(ChatResponse response) {
        return objectMapper.writeValueAsString(response);
    }

    public void fillHistory(ChatClient.AdvisorSpec advisorSpec, String sessionId) {
        // 1. 如果需要存储会话和消息到数据库，自己可以实现ChatMemory接口，这里使用自己实现的AiMessageChatMemory，数据库存储。
        // 2. 传入会话id，MessageChatMemoryAdvisor会根据会话id去查找消息。
        // 3. 只需要携带最近10条消息
        // MessageChatMemoryAdvisor会在消息发送给大模型之前，从ChatMemory中获取会话的历史消息，然后一起发送给大模型。
        advisorSpec.advisors(new MessageChatMemoryAdvisor(chatMemory, sessionId, 10));
    }

/*    public void useVectorStore(ChatClient.AdvisorSpec advisorSpec) {
        // question_answer_context是一个占位符，会替换成向量数据库中查询到的文档。QuestionAnswerAdvisor会替换。
        String promptWithContext = """
                下面是上下文信息
                ---------------------
                {question_answer_context}
                ---------------------
                给定的上下文和提供的历史信息，而不是事先的知识，回复用户的意见。如果答案不在上下文中，告诉用户你不能回答这个问题。
                """;
        advisorSpec.advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build(), promptWithContext));
    }*/

    public static String extractKeywords(String input) {
        int startIndex = input.lastIndexOf("关键字：");
        if (startIndex == -1) {
            return "";
        }
        startIndex += "关键字：".length();
        int endIndex = input.lastIndexOf("回答完毕");
        if (endIndex == -1) {
            endIndex = input.length();
        } else {
            endIndex -= "回答完毕".length();
        }
        if (startIndex >= endIndex) {
            return "";
        }
        return input.substring(startIndex, endIndex).trim();
    }

}