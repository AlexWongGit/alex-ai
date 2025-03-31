package org.alex.controller;

import lombok.extern.slf4j.Slf4j;
import org.alex.rag.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: RAG控制类
 * @Author wangzf
 * @Date 2025/2/27
 */
@RestController
@RequestMapping("rag")
public class RAGController {

    @Autowired
    private RagService ragService;


    @RequestMapping("ask")
    public String searchTallestSimilarity(@RequestParam("question") String question) {
        return ragService.performRag(question);
    }

}
