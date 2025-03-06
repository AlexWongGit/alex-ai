package org.alex.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public class MilvusUtil {


    public static List<Float> arcsoftToFloat(float[] floatArray) {
        List<Float> floatList = new ArrayList<>();
        for (float num : floatArray) {
            floatList.add(num);
        }
        return floatList;
    }


    public static byte[] convertEmbeddingsToBytes(List<float[]> embeddings) {
        // 计算所需的字节数
        int totalFloats = 0;
        for (float[] embedding : embeddings) {
            totalFloats += embedding.length;
        }
        // 每个 float 占 4 个字节
        int byteSize = totalFloats * 4;

        // 创建字节缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);

        // 将每个 float 转换为字节并存储到缓冲区中
        for (float[] embedding : embeddings) {
            for (float value : embedding) {
                buffer.putFloat(value);
            }
        }

        // 将缓冲区中的字节复制到字节数组中
        return buffer.array();
    }

    public static File convert(MultipartFile multipartFile) throws IOException {
        // 创建一个临时文件
        File tempFile = File.createTempFile("temp", null);
        // 将 MultipartFile 内容传输到临时文件
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
