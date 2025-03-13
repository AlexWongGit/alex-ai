package org.alex.fileprocess.parser;

import org.alex.common.enums.FileTypeEnum;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
public class PDFParser extends AbstractFileParser {

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 预处理文本，去除无用字符
            text = cleanText(text);

            // 分块处理
            chunks.addAll(splitText(text, maxChunkSize));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }




}

