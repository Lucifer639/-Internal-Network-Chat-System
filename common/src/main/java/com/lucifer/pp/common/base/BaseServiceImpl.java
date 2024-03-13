package com.lucifer.pp.common.base;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucifer.pp.common.auth.UserContext;
import jakarta.annotation.Resource;

import java.util.Collection;

public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M,T> implements BaseService<T> {

    @Resource
    protected DefaultIdentifierGenerator idGenerator;

    @Override
    public Long doAdd(T entity) {
        beforeAdd(entity);
        boolean flag = super.save(entity);
        if (flag){
            return entity.getId();
        }
        return null;
    }

    @Override
    public Long doUpdate(T entity){
        beforeUpdate(entity);
        super.updateById(entity);
        return entity.getId();
    }

    @Override
    public Long doRemove(T entity){
        beforeUpdate(entity);
        super.removeById(entity);
        return entity.getId();
    }

    @Override
    public boolean doAddBatch(Collection<T> entities) {
        entities.forEach(this::beforeAdd);
        return super.saveBatch(entities);
    }

    @Override
    public boolean doUpdateBatch(Collection<T> entities){
        entities.forEach(this::beforeUpdate);
        return super.updateBatchById(entities);
    }

    public void beforeAdd(T entity){
        if (ObjectUtil.isEmpty(entity.getId())){
            Long id = idGenerator.nextId(entity);
            entity.setId(id);
        }
        entity.setCreatedBy(UserContext.getUID());
    }

    public void beforeUpdate(T entity){
        entity.setUpdateBy(UserContext.getUID());
    }
}
