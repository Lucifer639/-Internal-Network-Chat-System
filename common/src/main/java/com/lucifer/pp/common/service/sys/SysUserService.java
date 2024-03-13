package com.lucifer.pp.common.service.sys;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.entity.sys.SysUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface SysUserService extends BaseService<SysUser> {
    List<String> getPermissionCode(Long uid);
}
