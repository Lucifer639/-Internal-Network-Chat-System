package com.lucifer.pp.common.service.sys.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.entity.sys.SysRole;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.mapper.sys.SysRoleMapper;
import com.lucifer.pp.common.mapper.sys.SysUserRoleMapper;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends BaseServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
    @Resource
    SysUserRoleMapper userRoleMapper;
    @Resource
    SysRoleMapper roleMapper;

    @Override
    public boolean hasRole(Long uid, String roleCode) {
        return ObjectUtil.isNotEmpty(userRoleMapper.getRoleIdByUIDAndRoleCode(uid,roleCode));
    }

    @Override
    public Long addRole(Long uid, String roleCode) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_code",roleCode);
        SysRole sysRole = roleMapper.selectOne(queryWrapper);
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setRoleId(sysRole.getId());
        sysUserRole.setUserId(uid);
        return this.doAdd(sysUserRole);
    }
}
