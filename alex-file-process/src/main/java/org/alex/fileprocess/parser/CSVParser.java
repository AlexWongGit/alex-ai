package org.alex.fileprocess.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alex.common.enums.FileTypeEnum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
public class CSVParser extends AbstractFileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        List<Map<String, String>> parsedData = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            // 空文件
            if (headerLine == null) {
                return chunks;
            }

            String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> rowData = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    rowData.put(headers[i], i < values.length ? values[i] : "");
                }

                parsedData.add(rowData);
            }

            // 转 JSON 字符串
            String jsonOutput = OBJECT_MAPPER.writeValueAsString(parsedData);

            // 分块存储
            chunks.addAll(splitText(jsonOutput, maxChunkSize));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }
}
