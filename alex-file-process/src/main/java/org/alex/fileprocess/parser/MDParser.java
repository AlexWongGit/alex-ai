package org.alex.fileprocess.parser;

import org.alex.common.enums.FileTypeEnum;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
public class MDParser extends AbstractFileParser {

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        List<String> sections = new ArrayList<>();
        StringBuilder currentSection = new StringBuilder();
        String currentTitle = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().matches("^#{1,6} .*")) { // 匹配 # 开头的标题
                    // 先保存之前的内容
                    if (currentTitle != null && currentSection.length() > 0) {
                        sections.add("Title: " + currentTitle + "\n" + currentSection.toString().trim());
                    }

                    // 处理新标题
                    currentTitle = line.trim();
                    currentSection.setLength(0); // 清空 StringBuilder
                } else {
                    currentSection.append(line).append("\n");
                }
            }

            // 保存最后一块内容
            if (currentTitle != null && currentSection.length() > 0) {
                sections.add("Title: " + currentTitle + "\n" + currentSection.toString().trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 预处理 & 分块存储
        for (String section : sections) {
            chunks.addAll(splitText(cleanText(section), maxChunkSize));
        }

        return chunks;
    }
}
