package com.lucifer.pp.common.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.entity.sys.SysPermissionRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysPermissionRoleMapper extends BaseMapper<SysPermissionRole> {
}
