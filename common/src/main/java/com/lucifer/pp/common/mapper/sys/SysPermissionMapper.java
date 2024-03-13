package com.lucifer.pp.common.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.entity.sys.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
}
