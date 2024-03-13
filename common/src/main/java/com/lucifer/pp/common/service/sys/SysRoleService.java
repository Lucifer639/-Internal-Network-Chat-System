package com.lucifer.pp.common.service.sys;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.entity.sys.SysRole;
import org.springframework.stereotype.Component;

@Component
public interface SysRoleService extends BaseService<SysRole> {
    SysRole findByRoleCode(String roleCode);
}
