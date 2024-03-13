package com.lucifer.pp.common.dto;

import com.lucifer.pp.common.entity.sys.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {
    private Long id;
    private String name;
    private String avatar;
    private Integer level;
    private String levelDescription;

    public static GroupMember generate(SysUser user){
        GroupMember groupMember = new GroupMember();
        groupMember.setId(user.getId());
        groupMember.setAvatar(user.getAvatar());
        groupMember.setName(user.getName());
        return groupMember;
    }
}
