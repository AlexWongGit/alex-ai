package org.alex.fileprocess.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/11
 */
public abstract class AbstractFileParser implements FileParser {

    public static final int MAX_CHUNK_SIZE = 1000;

    /**
     * 清理文本，去除空白、特殊字符等
     */
    @Override
    public String cleanText(String text) {
        // 去除制表符、回车符
        return text.replaceAll("[\\t\\r]+", "")
            // 合并多余换行
            .replaceAll("\\n{2,}", "\n")
            // 去除首尾空白
            .trim();
    }

    /**
     * 将文本按段落拆分，并限制每个块的最大长度
     */
    @Override
    public List<String> splitText(String text, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n");

        StringBuilder chunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            if (chunk.length() + paragraph.length() > maxChunkSize) {
                // 当前块已满，存入列表
                chunks.add(chunk.toString());
                chunk.setLength(0);
            }

            // 添加当前段落
            if (chunk.length() > 0) {
                chunk.append(" ");
            }
            chunk.append(paragraph);
        }

        // 添加最后一块内容
        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }

        return chunks;
    }

    /**
     * @description 判断文件是否为 OOXML 格式
     * @return boolean
     * @param file 文件
     */
    protected boolean isOOXML(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            // OOXML 文件都是 ZIP 格式（例如 .docx, .xlsx, .pptx）
            return isZipFormat(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @description 判断输入流是否为 ZIP 格式
     * @return boolean
     * @param inputStream 输入流
     */
    protected boolean isZipFormat(InputStream inputStream) throws IOException {
        byte[] signature = new byte[4];
        inputStream.read(signature);
        return (signature[0] == 'P' && signature[1] == 'K' && signature[2] == 3 && signature[3] == 4);
    }
}
