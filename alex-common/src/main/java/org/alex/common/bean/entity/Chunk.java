package org.alex.common.bean.entity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * @Author wangzf
 * @Date 2025/3/7
 */
@Builder
@Data
public class Chunk {
    private String id;
    // 切割后的文本
    private String text;
    // item1, item1a, item7, item7a
    private String item;
    // Chunk序列号
    private Integer chunkSeqId;
    // 属于的Form
    private String formId;
    // text的embedding
    private List<Double> textEmbedding;

}
