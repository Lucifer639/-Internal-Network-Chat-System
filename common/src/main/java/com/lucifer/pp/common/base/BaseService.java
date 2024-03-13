package com.lucifer.pp.common.base;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

public interface BaseService<T extends BaseEntity> extends IService<T> {

    Long doAdd(T entity);
    boolean doAddBatch(Collection<T> entities);
    Long doUpdate(T entity);
    boolean doUpdateBatch(Collection<T> entities);
    Long doRemove(T entity);
}
