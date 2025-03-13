package org.alex.fileprocess.parser;

import org.alex.common.enums.FileTypeEnum;
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;
import java.util.Base64;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

/**
 *
 * @Author wangzf
 * @Date 2025/3/12
 */
public class DOCParser extends AbstractFileParser {

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            String text;
            List<Map<String, String>> imageList = new ArrayList<>();

            if (isOOXML(file)) {
                text = parseDOCX(fis, imageList);
            } else {
                text = parseDOC(fis);
            }

            // 预处理文本
            text = cleanText(text);

            // 分块处理
            chunks.addAll(splitText(text, maxChunkSize));

            // 处理图片
            if (!imageList.isEmpty()) {
                chunks.add("Images: " + imageList);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }


    /**
     * 解析 `.docx`
     */
    private String parseDOCX(FileInputStream fis, List<Map<String, String>> imageList) throws IOException {
        StringBuilder content = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(fis)) {

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    for (XWPFRun run : paragraph.getRuns()) {
                        for (XWPFPicture picture : run.getEmbeddedPictures()) {
                            imageList.add(processImage(picture.getPictureData()));
                        }
                    }
                    content.append(paragraph.getText()).append("\n");
                } else if (element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;
                    content.append(parseTable(table)).append("\n");
                }
            }
        }
        return content.toString();
    }

    /**
     * 解析 `.doc`（OLE2格式）
     */
    private String parseDOC(FileInputStream fis) throws IOException {
        try (HWPFDocument document = new HWPFDocument(fis)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * 处理 Word 表格
     */
    private String parseTable(XWPFTable table) {
        StringBuilder tableContent = new StringBuilder();
        for (XWPFTableRow row : table.getRows()) {
            List<String> rowValues = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                rowValues.add(cell.getText().trim());
            }
            tableContent.append(String.join(" | ", rowValues)).append("\n");
        }
        return tableContent.toString();
    }

    /**
     * 处理图片（Base64 或存入本地）
     */
    private Map<String, String> processImage(XWPFPictureData pictureData) {
        Map<String, String> imageInfo = new HashMap<>();
        String extension = pictureData.suggestFileExtension();
        byte[] bytes = pictureData.getData();
        String base64 = Base64.getEncoder().encodeToString(bytes);

        imageInfo.put("type", extension);
        imageInfo.put("base64", base64);
        return imageInfo;
    }
}
