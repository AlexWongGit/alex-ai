package org.alex.common.bean.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description 基础类
 * @author wangzf
 * @date 2025/3/31
 */
@Data
@ApiModel(value = "基础类")
public class BaseEntity implements Serializable {
    public static final String CONTROL = "100";
    public static final String STATUS_NORMAL = "0";
    public static final String STATUS_DELETE = "1";
    public static final String STATUS_DISABLE = "2";

    public static final String ID_SEPARATOR = ",";
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;


    /**
     * 创建人用户名
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人用户名")
    private String createUser;
    /**
     * 创建日期
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建日期")
    private LocalDateTime createTime;


    /**
     * 更新人用户名
     */
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新人用户名")
    private String updateUser;
    /**
     * 更新日期
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE, update = "now()")
    @ApiModelProperty(value = "更新日期")
    private LocalDateTime updateTime;

    public static final String COL_ID = "id";

    public static final String COL_CREATE_USER = "create_user";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_USER = "update_user";

    public static final String COL_UPDATE_TIME = "update_time";

}
