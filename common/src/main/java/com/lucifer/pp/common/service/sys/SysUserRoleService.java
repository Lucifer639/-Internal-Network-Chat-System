package com.lucifer.pp.common.service.sys;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import org.springframework.stereotype.Component;

@Component
public interface SysUserRoleService extends BaseService<SysUserRole> {
    boolean hasRole(Long uid, String roleCode);
}
