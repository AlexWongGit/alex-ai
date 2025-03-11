package org.alex.rag.module.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Tool
@Description("机器的cpu，文件夹相关信息")
@AllArgsConstructor
public class DevTool extends AbstractTool implements Function<DevTool.Request, String> {
    private final ChatModel chatModel;

    @Override
    public String apply(Request request) {
        return ChatClient.create(chatModel)
                .prompt()
                .tools(getFunctions(CPUTool.class, DirectoryReaderTool.class))
                .user(request.query())
                .call()
                .content();
    }

    public record Request(
            @JsonProperty(required = true) @JsonPropertyDescription(value = "用户的提问") String query) {
    }


}