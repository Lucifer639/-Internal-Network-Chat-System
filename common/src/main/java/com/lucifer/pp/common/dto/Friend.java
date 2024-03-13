package com.lucifer.pp.common.dto;

import com.lucifer.pp.common.entity.sys.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    private Long id;
    private String userCode;
    private String name;
    private String avatar;
    private boolean isOnline;

    public static Friend generate(SysUser user){
        Friend friend = new Friend();
        friend.setAvatar(user.getAvatar());
        friend.setId(user.getId());
        friend.setUserCode(user.getUserCode());
        friend.setName(user.getName());
        return friend;
    }
}
