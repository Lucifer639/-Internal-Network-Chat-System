package com.lucifer.pp.common.service.sys.impl;

import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.mapper.sys.SysUserMapper;
import com.lucifer.pp.common.service.sys.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends BaseServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper userMapper;

    @Override
    public List<String> getPermissionCode(Long uid) {
        return userMapper.getPermissionCode(uid);
    }

}
