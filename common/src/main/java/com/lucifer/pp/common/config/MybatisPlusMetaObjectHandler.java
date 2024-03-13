package com.lucifer.pp.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"version",Long.class,1L);
        this.strictInsertFill(metaObject,"status",Integer.class, BaseConstant.ENTITY_STATUS_VALID);
        this.strictInsertFill(metaObject,"createdDt",Long.class,System.currentTimeMillis());
        this.strictInsertFill(metaObject,"updateDt",Long.class,System.currentTimeMillis());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"updateDt",Long.class,System.currentTimeMillis());
    }
}
