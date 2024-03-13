package com.lucifer.pp.common.service.sys.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.mapper.sys.SysUserRoleMapper;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends BaseServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
    @Resource
    SysUserRoleMapper userRoleMapper;

    @Override
    public boolean hasRole(Long uid, String roleCode) {
        return ObjectUtil.isNotEmpty(userRoleMapper.getRoleIdByUIDAndRoleCode(uid,roleCode));
    }
}
