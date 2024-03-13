package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.net.data.CreateGroupResponseData;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CreateGroupResponseFunction implements PPFunction{

    private final static PPProtocolEnum protocol = PPProtocolEnum.CREATE_GROUP_RESPONSE;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        CreateGroupResponseData data = ((JSONObject) o).toBean(CreateGroupResponseData.class);
        Group group = new Group();
        group.setId(data.getGroupId());
        group.setName(data.getGroupName());
        group.setAvatar(null);
        group.setMembers(new ArrayList<>());
        GroupMember groupMember = new GroupMember(PPClientContext.uid,PPClientContext.name,
                PPClientContext.avatar, GroupMemberLevel.LEADER.level, GroupMemberLevel.LEADER.levelDescription);
        group.getMembers().add(groupMember);
        PPClientContext.groups.add(group);
        return null;
    }
}
