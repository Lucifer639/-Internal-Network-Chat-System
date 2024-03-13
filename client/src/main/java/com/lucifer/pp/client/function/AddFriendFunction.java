package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.gui.controller.ClientTabPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddFriendFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.ADD_FRIEND;
    private final ClientTabPaneController clientTabPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        Friend friend = ((JSONObject) o).toBean(Friend.class);
        PPClientContext.friends.add(friend);
        clientTabPaneController.friendList.refresh();
        return ChannelContext.release();
    }
}
