package org.alex.common.config;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public static final String DEFAULT_NAME = "system";

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createUser", String.class, DEFAULT_NAME);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateUser", String.class, DEFAULT_NAME);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateUser", String.class, DEFAULT_NAME);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}


