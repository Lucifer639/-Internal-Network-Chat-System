package com.lucifer.pp.net.netenum;

public enum ChatEnum {
    FRIEND(0),
    GROUP(1);
    public Integer code;
    ChatEnum(Integer code){
        this.code = code;
    }
}
