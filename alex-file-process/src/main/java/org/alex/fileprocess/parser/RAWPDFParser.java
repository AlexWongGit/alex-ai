package org.alex.fileprocess.parser;

import org.alex.common.bean.entity.file.ParsedResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/17
 */
public class RAWPDFParser {

    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 解析 PDF 文件，提取文本、图片，并异步执行 OCR
     */
    public ParsedResult parsePdf(File file) throws IOException, InterruptedException, ExecutionException {
        ParsedResult result = new ParsedResult();
        PDDocument document = PDDocument.load(file);
        try {
            // 1. 提取文本
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            // 对文本进行预处理（例如去掉多余换行、空白等）
            String cleanText = cleanText(rawText);
            result.setText(cleanText);

            // 2. 提取图片：使用 PDFRenderer 渲染每一页为图片
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            List<BufferedImage> images = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, 150); // 150 DPI，可调整分辨率
                images.add(img);
            }
            result.setImages(images);

            // 3. 异步 OCR 处理每个图片（这里只是示例，不做实际 OCR）：
            List<String> ocrResults;
            List<CompletableFuture<String>> futures = images.stream().map(img -> CompletableFuture.supplyAsync(() -> {
                // 这里可以调用 OCR 引擎，例如：
                // return tesseract.doOCR(img);
                // 为示例返回 dummy 文本
                return "Detected text from image";
            }, taskExecutor)).toList();

            // 等待所有异步任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            // 阻塞等待所有任务完成
            allFutures.join();
            ocrResults = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            result.setOcrText(ocrResults);

            // 4. 结构化处理：此处可以调用布局识别、表格提取等方法（示例中以 stub 形式呈现）
            // result.setLayoutData(layoutRecognition(images, cleanText));
            // result.setTableData(tableExtraction(images));

            // 5. 分块处理：例如将文本分块，每块不超过指定字数
            List<String> textChunks = splitText(cleanText, 1000);
            result.setTextChunks(textChunks);

        } finally {
            document.close();
        }
        return result;
    }

    /**
     * 简单的文本清理方法
     */
    private String cleanText(String text) {
        // 去除多余空格、换行符等
        return text.replaceAll("[\\t\\r]+", "").replaceAll("\\n{2,}", "\n").trim();
    }

    /**
     * 将文本按每块最大长度分块
     */
    private List<String> splitText(String text, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += maxChunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + maxChunkSize)));
        }
        return chunks;
    }


}
