package org.alex.fileprocess.parser;

import org.alex.common.enums.FileTypeEnum;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.*;
import java.util.*;

/**
 *
 * @Author wangzf
 * @Date 2025/3/13
 */
public class PPTParser extends AbstractFileParser {

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        try {

            if (isOOXML(file)) {
                chunks.addAll(parsePPTX(file, maxChunkSize));
            } else {
                chunks.addAll(parsePPT(file, maxChunkSize));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }


    /** 解析 PPTX 文件 */
    private List<String> parsePPTX(File file, int maxChunkSize) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder textContent = new StringBuilder();
        List<Map<String, String>> imageList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
            XMLSlideShow ppt = new XMLSlideShow(fis)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                // 获取幻灯片标题
                if (slide.getTitle() != null) {
                    textContent.append(slide.getTitle()).append("\n");
                }

                // 遍历文本段落
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof XSLFTextShape textShape) {
                        textShape.getTextParagraphs().forEach(paragraph ->
                            paragraph.getTextRuns().forEach(run ->
                                textContent.append(run.getRawText()).append(" ")
                            )
                        );
                        textContent.append("\n");
                    }
                });

                // 处理图片
                for (XSLFPictureData pictureData : ppt.getPictureData()) {
                    imageList.add(processImage(pictureData.getData(), pictureData.suggestFileExtension()));
                }
            }
        }

        String text = cleanText(textContent.toString());
        chunks.addAll(splitText(text, maxChunkSize));

        if (!imageList.isEmpty()) {
            chunks.add("Images: " + imageList);
        }

        return chunks;
    }


    /** 解析 PPT 文件 */
    private List<String> parsePPT(File file, int maxChunkSize) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder textContent = new StringBuilder();
        List<Map<String, String>> imageList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
            HSLFSlideShow ppt = new HSLFSlideShow(new POIFSFileSystem(fis))) {

            for (HSLFSlide slide : ppt.getSlides()) {
                if (slide.getTitle() != null) {
                    textContent.append(slide.getTitle()).append("\n");
                }

                // 获取文本
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof HSLFTextShape textShape) {
                        textContent.append(textShape.getText()).append("\n");
                    }
                });

                // 处理图片
                for (HSLFPictureData pictureData : ppt.getPictureData()) {
                    imageList.add(processImage(pictureData.getData(), pictureData.getType().name()));
                }
            }
        }

        String text = cleanText(textContent.toString());
        chunks.addAll(splitText(text, maxChunkSize));

        if (!imageList.isEmpty()) {
            chunks.add("Images: " + imageList);
        }

        return chunks;
    }


    /** 处理图片数据 */
    private Map<String, String> processImage(byte[] imageData, String extension) {
        Map<String, String> imageInfo = new HashMap<>();
        String base64 = Base64.getEncoder().encodeToString(imageData);

        imageInfo.put("type", extension);
        imageInfo.put("base64", base64);
        return imageInfo;
    }
}
