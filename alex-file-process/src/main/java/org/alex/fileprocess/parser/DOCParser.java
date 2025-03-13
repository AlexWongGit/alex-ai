package org.alex.fileprocess.parser;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;
import java.util.Base64;

public class DOCParser extends AbstractFileParser {

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();

        if (maxChunkSize <= 0 || maxChunkSize > MAX_CHUNK_SIZE) {
            maxChunkSize = MAX_CHUNK_SIZE;
        }

        try (FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder content = new StringBuilder();
            List<Map<String, String>> imageList = new ArrayList<>();

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    for (XWPFRun run : paragraph.getRuns()) {
                        for (XWPFPicture picture : run.getEmbeddedPictures()) {
                            imageList.add(processImage(picture.getPictureData()));
                        }
                    }
                    content.append(paragraph.getText()).append("\n");
                }
            }

            // 预处理文本
            String text = cleanText(content.toString());

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

    private Map<String, String> processImage(XWPFPictureData pictureData) {
        Map<String, String> imageInfo = new HashMap<>();
        String extension = pictureData.suggestFileExtension();
        byte[] bytes = pictureData.getData();
        String base64 = Base64.getEncoder().encodeToString(bytes);

        imageInfo.put("type", extension);
        // 或者存入本地文件系统
        imageInfo.put("base64", base64);
        return imageInfo;
    }
}
