package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.gui.controller.ClientTabPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DelFriendFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.DELETE_FRIEND;
    private final ClientTabPaneController clientTabPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        Long uid = (Long) o;
        Optional<Friend> first = PPClientContext.friends.stream().
                filter(friend -> friend.getId().equals(uid)).findFirst();
        first.ifPresent(friend -> {
            PPClientContext.friends.remove(friend);
            clientTabPaneController.friendList.refresh();
        });
        return ChannelContext.release();
    }
}
