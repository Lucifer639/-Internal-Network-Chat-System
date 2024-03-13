package com.lucifer.pp.net.netenum;

public enum GroupMemberLevel {
    LEADER(0,"群主"),
    MANAGER(1,"管理员"),
    MEMBER(2,"成员");
    public Integer level;
    public String levelDescription;
    GroupMemberLevel(Integer level,String levelDescription){
        this.level = level;
        this.levelDescription = levelDescription;
    }
}
