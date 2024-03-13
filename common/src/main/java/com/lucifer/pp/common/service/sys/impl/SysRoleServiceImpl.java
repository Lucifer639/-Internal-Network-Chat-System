package com.lucifer.pp.common.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.entity.sys.SysRole;
import com.lucifer.pp.common.mapper.sys.SysRoleMapper;
import com.lucifer.pp.common.service.sys.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends BaseServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Override
    public SysRole findByRoleCode(String roleCode) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_code",roleCode);
        return this.getOne(queryWrapper);
    }
}
