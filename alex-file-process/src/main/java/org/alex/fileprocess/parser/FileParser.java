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

    List<String> split2Chunks(File file,int maxChunkSize, FileTypeEnum fileType);

    String cleanText(String text);

    List<String> splitText(String text, int maxChunkSize);


}
