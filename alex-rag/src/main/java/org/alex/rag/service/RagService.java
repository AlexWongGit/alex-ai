package org.alex.rag.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public interface RagService {
    Flux<ChatResponse> performRag(String question);

    Boolean uploadFileAndSaveToMilvus(MultipartFile file) throws IOException;

}
