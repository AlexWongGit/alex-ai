package org.alex.rag.module.prompt;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/10
 */
public interface PromptTemplateConstants {

    String GET_KEYWORDS_TEMPLATE = "请提取这个句子中{question}的关键词，结果只要是一个字符串以'关键词：" +
        "'开头，用'、'分割，并以'回答完毕'结尾，不要有多余的话，注意只需要这个句子中包含的关键词即可。";

    String PROMPT_RAG = """
          严格参照所提供的上下文回答查询:
          {context}
          问题:
          {query}
         如果你从文中找不到答案，就说：
              很抱歉，我没有你要找的信息。
        """;
}
