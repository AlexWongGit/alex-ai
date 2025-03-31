package org.alex.common.bean.entity.file;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/17
 */
public class ParsedResult {

    private String text;
    private List<BufferedImage> images;
    private List<String> ocrText;
    private List<String> textChunks;
    // 可扩展其他结构化数据，如 layoutData、tableData 等

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<BufferedImage> getImages() {
        return images;
    }

    public void setImages(List<BufferedImage> images) {
        this.images = images;
    }

    public List<String> getOcrText() {
        return ocrText;
    }

    public void setOcrText(List<String> ocrText) {
        this.ocrText = ocrText;
    }

    public List<String> getTextChunks() {
        return textChunks;
    }

    public void setTextChunks(List<String> textChunks) {
        this.textChunks = textChunks;
    }
}
