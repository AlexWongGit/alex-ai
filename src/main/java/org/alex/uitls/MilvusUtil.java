package org.alex.uitls;

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

    public static List<Float> arcsoftToFloat(byte[] arcsoftFeature) {
        // 检查输入的字节数组是否为空或长度是否是 4 的倍数
        if (arcsoftFeature == null || arcsoftFeature.length % 4!= 0) {
            throw new IllegalArgumentException("输入的字节数组长度必须是 4 的倍数");
        }

        // 创建一个存储 Float 类型数据的列表
        List<Float> floatList = new ArrayList<>();

        // 循环遍历字节数组，每 4 个字节转换为一个 float 类型的数据
        for (int i = 0; i < arcsoftFeature.length; i += 4) {
            // 将 4 个字节组合成一个 int 类型的数据
            int intBits =
                    ((arcsoftFeature[i] & 0xFF) << 24) |
                            ((arcsoftFeature[i + 1] & 0xFF) << 16) |
                            ((arcsoftFeature[i + 2] & 0xFF) << 8) |
                            (arcsoftFeature[i + 3] & 0xFF);

            // 将 int 类型的数据转换为 float 类型的数据
            float floatValue = Float.intBitsToFloat(intBits);

            // 将转换后的 float 类型的数据添加到列表中
            floatList.add(floatValue);
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
