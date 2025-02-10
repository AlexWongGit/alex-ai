package org.alex.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.alex.entity.Dto;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("ai")
public class ChatController {

  /*  @Autowired
    private PoetryService poetryService;
*/
    @Value("${ai.api_key}")
    private String apiKey;


    private final OllamaChatModel chatModel;

    @Autowired
    public ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }


/*    @GetMapping("/cathaiku")
    public ResponseEntity<String> generateHaiku() {
        return ResponseEntity.ok(poetryService.getCatHaiku());
    }*/

/*    @GetMapping("/chat")
    public ResponseEntity<String> chat(String message) {
        String systemPrompt = "{prompt}";
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        String userPrompt = message;
        Message userMessage = new UserMessage(userPrompt);
        Map<String, Object> map = new HashMap<>();
        map.put("prompt", "you are a helpful AI assistant");
        Message systemMessage = systemPromptTemplate.createMessage(map);

        List<Message> list = new ArrayList<>();
        list.add(userMessage);
        list.add(systemMessage);
        Prompt prompt = new Prompt(list);
        Flux<String> response =chatClient.stream(prompt).flatMap(res->{
            List<Generation> generations = res.getResults();
            if(CollectionUtils.isNotEmpty(generations)){
                for(Generation generation:generations){
                    AssistantMessage assistantMessage =  generation.getOutput();
                    String content = assistantMessage.getContent();
                    System.out.println(content);
                    return Flux.just(content);
                }
            }
            return Flux.empty();
        });
    }*/

   /* @PostMapping("ask")
    public String ask(@RequestBody Dto dto) throws Exception {

        Generation generation = new Generation();

        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(dto.getAsk())
                .build();

        GenerationParam param = GenerationParam.builder()
                .model("qwen-turbo")
                //.model("deepseek-chat")
                .messages(Arrays.asList(userMessage))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .apiKey(apiKey)
                .enableSearch(true)
                .build();
        GenerationResult generationResult = generation.call(param);
*//*        Flowable<GenerationResult> generationResultFlowable = generation.streamCall(param);
        generation.streamCall(param, new ResultCallback<GenerationResult>() {

            @Override
            public void onEvent(GenerationResult generationResult) {
                // 处理每个GenerationResult对象
                System.out.println("Received: " + generationResult.getOutput().getChoices().get(0).getMessage().getContent());

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                // 数据流完成
                System.out.println("Generation completed.");
            }
        });*//*
        return generationResult.getOutput().getChoices().get(0).getMessage().getContent();

    }*/

    @PostMapping("chat")
    public String cc(@RequestBody Dto dto) throws Exception {
        String baseUrl = "https://api.deepseek.com/chat/completions";
        HashMap<String, String> headers = new HashMap<>(2);
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        String ask = new JSONObject()
                .put("model", "deepseek-chat")
                .put("frequency_penalty", 2)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", dto.getAsk())))
                .put("stream", false)
                .toString();
        HttpResponse httpResponse = HttpRequest.post(baseUrl).headerMap(headers, true).body(ask).execute();
        if (httpResponse.isOk()) {
            return httpResponse.body();
        }
        return null;
    }


    @PostMapping("deepseek")
    public HashMap<Object, Object> deepseek(@RequestBody Dto dto) {
        HashMap<Object, Object> map = new HashMap<>();
        try {
            OllamaOptions ollamaOptions = new OllamaOptions();
            ollamaOptions.setModel("deepseek-chat");
            ollamaOptions.setTemperature(0.4);

            ChatResponse response = chatModel.call(
                    new Prompt(dto.getAsk(), ollamaOptions));
            map.put("deepseek", response);
        } catch (Exception e) {
            map.put("error", "请求发生错误：" + e.getMessage());
        }
        return map;
    }

}
