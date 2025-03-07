package org.alex.rag.service.impl;

import org.alex.dataprocess.service.FileService;
import org.alex.common.entity.ArchiveDto;
import org.alex.rag.service.RagService;
import org.alex.common.utils.MilvusUtil;
import org.alex.vec.service.MilvusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private final OllamaEmbeddingModel embeddingModel;
    private final OllamaChatModel ollamaChatClient;

    private final MilvusService milvusService;

    private final FileService fileService;

    public RagServiceImpl(OllamaEmbeddingModel embeddingModel, OllamaChatModel ollamaChatClient, MilvusService milvusService, FileService fileService) {
        this.embeddingModel = embeddingModel;
        this.ollamaChatClient = ollamaChatClient;
        this.milvusService = milvusService;
        this.fileService = fileService;
    }

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
    public String performRag(String question) {
        // 步骤 1: 使用大模型提取关键词生成向量
        final String template = "请提取这个句子中{question}最适合用来转化成向量作为向量数据库搜索条件的关键词，结果只要是一个字符串以关键字是开头，不要有多余的话。";
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt1 = promptTemplate.create(Map.of("question", question));

        ChatResponse chatResponse = ollamaChatClient.call(prompt1);

        String keyWords = chatResponse.getResult().toString();

        // 步骤 2: 生成关键字的嵌入向量
        float[] embedding = embeddingModel.embed(keyWords);

        // 步骤 3: 数据检索阶段, 在 Milvus 中搜索最相似的文档
        String searchResult = milvusService.searchSimilarity(embedding, null, keyWords);

        // 步骤 4: 信息增强与整合

        // 步骤 5: 将搜索结果和问题一起发送给推理模型
        String context = searchResult != null? searchResult : "";
        String systemPrompt = "你需要根据提供的上下文准确回答用户的问题。";
        String userPrompt = "问题: " + question + "\n上下文: " + context;

        List<Message> messages = Arrays.asList(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        );
        Prompt prompt2 = new Prompt(messages);


        // 步骤 6: 自然语言生成, 获取模型生成的答案transformer、rnn
        ChatResponse call = ollamaChatClient.call(prompt2);

        // 步骤 7: 动态融合与输出 attention

        // 步骤 8: 返回结果
        return call.getResult().toString();
    }

}