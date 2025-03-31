package org.alex.common.bean.entity.file;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.alex.common.bean.entity.BaseEntity;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/31
 */
@Data
@TableName("rag_file_images")
public class RagFileImages extends BaseEntity {

    @TableField(value = "file_id")
    private String fileId;

    @TableField(value = "image_url")
    private String imageUrl;
}
