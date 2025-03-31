package org.alex.common.bean.entity.file;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.alex.common.bean.entity.BaseEntity;

/**
 * @Description:  RAGFile
 * @Author wangzf
 * @Date 2025/3/11
 */
@Data
@TableName("rag_file")
public class RAGFile extends BaseEntity {

    @TableField(value = "file_name")
    private String fileName;

    @TableField(value = "file_url")
    private String fileUrl;

    @TableField(value = "file_type")
    private String fileType;

}
