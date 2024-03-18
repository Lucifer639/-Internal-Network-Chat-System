package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.UpdateMemberData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateGroupMemberFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.UPDATE_GROUP_MEMBER;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        UpdateMemberData data = ((JSONObject) o).toBean(UpdateMemberData.class);
        Optional<Group> groupOptional = PPClientContext.groups.stream()
                .filter(group -> group.getId().equals(data.getGroupId()))
                .findFirst();
        if (groupOptional.isEmpty()) return null;
        Optional<GroupMember> groupMemberOptional = groupOptional.get().getMembers().stream()
                .filter(groupMember -> groupMember.getId().equals(data.getGroupMember().getId()))
                .findFirst();
        if (groupMemberOptional.isEmpty()) return null;
        GroupMember groupMember = groupMemberOptional.get();
        groupMember.setAvatar(data.getGroupMember().getAvatar());
        groupMember.setLevel(data.getGroupMember().getLevel());
        groupMember.setLevelDescription(data.getGroupMember().getLevelDescription());
        groupMember.setName(data.getGroupMember().getName());
        return ChannelContext.release();
    }
}
