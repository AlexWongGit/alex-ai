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
public class TXTParser extends AbstractFileParser {

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 预处理文本（去掉无用字符）
        String text = cleanText(content.toString());

        // 分块处理
        chunks.addAll(splitText(text, maxChunkSize));

        return chunks;
    }
}

