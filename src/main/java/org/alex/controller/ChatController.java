package org.alex.controller;


import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import org.alex.entity.Dto;
import org.alex.service.PoetryService;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("ai")
public class ChatController {

    @Autowired
    private PoetryService poetryService;

    @Value("${ai.api_key}")
    private String apiKey;


    @GetMapping("/cathaiku")
    public ResponseEntity<String> generateHaiku() {
        return ResponseEntity.ok(poetryService.getCatHaiku());
    }

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

    @PostMapping("ask")
    public String ask(@RequestBody Dto dto) throws Exception {

        Generation generation = new Generation();

        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(dto.getAsk())
                .build();

        GenerationParam param = GenerationParam.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(userMessage))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .apiKey(apiKey)
                .enableSearch(true)
                .build();
        GenerationResult generationResult =generation.call(param);
/*        Flowable<GenerationResult> generationResultFlowable = generation.streamCall(param);
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
        });*/
        return generationResult.getOutput().getChoices().get(0).getMessage().getContent();

    }

}
