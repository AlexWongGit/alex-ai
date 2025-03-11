package org.alex.dataprocess.processor;

import java.io.File;
import java.util.List;

/**
 *
 * @Author wangzf
 * @Date 2025/3/11
 */
public interface FileParser {

    List<String> split2Chunks(File file,int maxChunkSize);

    String getFileName(File file);

    String cleanText(String text);

    List<String> splitText(String text, int maxChunkSize);


}
