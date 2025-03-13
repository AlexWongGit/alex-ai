package org.alex.fileprocess.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alex.common.enums.FileTypeEnum;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
public class XMLParser extends AbstractFileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        try {
            // 解析 XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            List<Map<String, Object>> parsedData = new ArrayList<>();
            parseElement(document.getDocumentElement(), parsedData, new LinkedList<>());

            // 转 JSON 字符串
            String jsonOutput = OBJECT_MAPPER.writeValueAsString(parsedData);

            // 分块存储
            chunks.addAll(splitText(jsonOutput, maxChunkSize));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chunks;
    }

    private void parseElement(Element element, List<Map<String, Object>> parsedData, LinkedList<String> path) {
        Map<String, Object> nodeData = new HashMap<>();
        path.add(element.getTagName());

        // 获取文本内容
        String textContent = element.getTextContent().trim();
        if (!textContent.isEmpty()) {
            nodeData.put("path", String.join(" > ", path));
            nodeData.put("content", textContent);
            parsedData.add(nodeData);
        }

        // 递归解析子节点
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                parseElement((Element) node, parsedData, path);
            }
        }

        path.removeLast();
    }
}
