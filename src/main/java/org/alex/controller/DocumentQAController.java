package org.alex.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO 功能描述,换行请使用标签<br>
 *
 * @Author xxx （自己的名）
 * @Date 2025/2/12
 */
@RestController
@RequestMapping("doc")
public class DocumentQAController {
    private static final String API_BASE_URL = "http://localhost:48081";

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @PostMapping("upload")
    public String uploadFile(@RequestPart("file") MultipartFile file) {
        try {
            File tempFile = File.createTempFile("upload", file.getOriginalFilename());
            file.transferTo(tempFile);
            String apiUrl = API_BASE_URL + "/upload?file_path=" + tempFile.getAbsolutePath();
            HttpResponse httpResponse = HttpRequest.post(apiUrl).execute();
            tempFile.delete();
            return httpResponse.body();
        } catch (IOException e) {
            System.err.println("上传文件失败：" + e.getMessage());
            return "上传文件失败：" + e.getMessage();
        }
    }


    @PostMapping("batchUpload")
    public String batchUploadFile(@RequestPart("files") MultipartFile[] files) {
        try {
            List<File> tempFiles = new ArrayList<>();
            ArrayNode filePathsArray = objectMapper.createArrayNode();
            for (MultipartFile file : files) {
                File tempFile = File.createTempFile("upload", file.getOriginalFilename());
                file.transferTo(tempFile);
                filePathsArray.add(tempFile.getAbsolutePath());
                tempFiles.add(tempFile);
            }
            String jsonBody = objectMapper.writeValueAsString(filePathsArray);

            String apiUrl = API_BASE_URL + "/upload_batch";
            HttpResponse httpResponse = HttpRequest.post(apiUrl).body(jsonBody).execute();
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            return httpResponse.body();
        } catch (IOException e) {
            System.err.println("上传文件失败：" + e.getMessage());
            return "上传文件失败：" + e.getMessage();
        }
    }

    @GetMapping("ask")
    public String askQuestion(@RequestParam("question") String question) throws JsonProcessingException {
        String apiUrl = API_BASE_URL + "/ask?question=" + question;
        HttpResponse httpResponse = HttpRequest.post(apiUrl).execute();
        String responseBody = httpResponse.body();

        Map<String, Object> jsonResponse = objectMapper.readValue(responseBody, Map.class);
        String result = null;
        if (jsonResponse.containsKey("answer")) {
            result =  "answer: " + jsonResponse.get("answer") + "\n" + "sources: " + jsonResponse.get("sources");
        } else if (jsonResponse.containsKey("error")) {
            result =  "error: " + jsonResponse.get("error");
        }
        return result;
    }
}
