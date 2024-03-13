package com.lucifer.pp.net.netenum;

public enum ApplyType {

    APPLY_FRIEND(0,"applyFriend"),
    APPLY_GROUP(1,"applyGroup"),
    INVITE_GROUP(2,"inviteGroup");
    public Integer code;
    public String description;

    ApplyType(int code, String description){
        this.code = code;
        this.description = description;
    }
}
