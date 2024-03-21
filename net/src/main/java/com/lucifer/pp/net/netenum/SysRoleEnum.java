package com.lucifer.pp.net.netenum;

public enum SysRoleEnum {
    ROOT(1,"ROOT","超级管理员"),
    GROUP_LEADER(2,"GROUP_LEADER","群主"),
    GROUP_MANAGER(3,"GROUP_MANAGER","群管理员"),
    NORMAL(4,"NORMAL","普通用户");
    public Integer id;
    public String roleCode;
    public String name;
    SysRoleEnum(Integer id,String roleCode,String name){
        this.id = id;
        this.roleCode = roleCode;
        this.name = name;
    }
}
