package com.lucifer.pp.common.dto;

import com.lucifer.pp.common.entity.pp.PPGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    private Long id;
    private String name;
    private String avatar;
    private List<GroupMember> members;

    public static Group generate(PPGroup ppGroup){
        Group group = new Group();
        group.setId(ppGroup.getId());
        group.setName(ppGroup.getName());
        group.setAvatar(ppGroup.getAvatar());
        return group;
    }
}
