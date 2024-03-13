package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.gui.controller.ClientTabPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class AddGroupMemberFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.ADD_GROUP_MEMBER;
    private final ClientTabPaneController clientTabPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        Group data = ((JSONObject) o).toBean(Group.class);
        Optional<Group> targetGroup = PPClientContext.groups.stream().filter(group -> group.getId().equals(data.getId())).findFirst();
        targetGroup.ifPresent(group -> {
            group.getMembers().addAll(data.getMembers());
        });
        clientTabPaneController.groupList.refresh();
        return ChannelContext.release();
    }
}
