package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.gui.controller.ClientTabPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.DelGroupMemberData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DelGroupMemberFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.DELETE_GROUP_MEMBER;
    private final ClientTabPaneController clientTabPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        DelGroupMemberData data = ((JSONObject) o).toBean(DelGroupMemberData.class);
        Optional<Group> optionalGroup = PPClientContext.groups.stream()
                .filter(group -> group.getId().equals(data.getGid())).findFirst();
        optionalGroup.ifPresent(group -> {
            if (PPClientContext.uid.equals(data.getUid())){
                PPClientContext.groups.remove(group);
                clientTabPaneController.groupList.refresh();
            }else{
                Optional<GroupMember> optionalGroupMember = group.getMembers().stream().
                        filter(member -> member.getId().equals(data.getUid())).findFirst();
                optionalGroupMember.ifPresent(member -> group.getMembers().remove(member));
            }
        });
        return ChannelContext.release();
    }
}
