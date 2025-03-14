package org.alex.fileprocess.parser;

import org.alex.common.enums.FileTypeEnum;

import java.io.File;
import java.util.List;

/**
 *
 * @Author wangzf
 * @Date 2025/3/11
 */
public interface FileParser {

    /**
     * @description 文件解析成文本块
     * @return java.util.List<java.lang.String>
     * @param file 文件
     * @param maxChunkSize 最大文本块大小
     * @param fileType 文件类型
     */
    List<String> split2Chunks(File file,int maxChunkSize, FileTypeEnum fileType);

    /**
     * @description 清理文本
     * @return java.lang.String
     * @param text
     */
    String cleanText(String text);

    /**
     * @description 拆分文本
     * @return java.util.List<java.lang.String>
     * @param text
     * @param maxChunkSize
     */
    List<String> splitText(String text, int maxChunkSize);


}
