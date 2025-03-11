package org.alex.rag.module.tool;

import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Description("CPU的核数")
public class CPUTool implements Function<CPUTool.Request, Integer> {

    @Override
    public Integer apply(CPUTool.Request request) {
        return Runtime.getRuntime().availableProcessors();
    }

    public record Request() {
    }
}
