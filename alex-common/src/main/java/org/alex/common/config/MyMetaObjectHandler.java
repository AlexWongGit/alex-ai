package org.alex.common.config;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @description 自定义元对象处理器
 * @author wangzf
 * @date 2025/3/31
 */
@Component
@Slf4j
@NoArgsConstructor
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String userName = null;
        String realname = null;

        this.strictInsertFill(metaObject, "createBy", String.class, userName == null ? "system" : userName);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", String.class, userName == null ? "system" : userName);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        if (StringUtils.isNotBlank(metaObject.findProperty("createName", true))) {
            this.strictInsertFill(metaObject, "createName", String.class, realname == null ? "system" : realname);
        }
        if (StringUtils.isNotBlank(metaObject.findProperty("updateName", true))) {
            this.strictInsertFill(metaObject, "updateName", String.class, realname == null ? "system" : realname);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userName = null;
        String realname = null;
        this.strictUpdateFill(metaObject, "updateBy", String.class, userName == null ? "system" : userName);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        if (StringUtils.isNotBlank(metaObject.findProperty("updateName", true))) {
            this.strictUpdateFill(metaObject, "updateName", String.class, realname == null ? "system" : realname);
        }
    }
}


