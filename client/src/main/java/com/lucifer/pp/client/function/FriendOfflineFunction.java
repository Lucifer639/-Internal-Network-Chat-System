package com.lucifer.pp.client.function;

import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.gui.controller.ClientTabPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FriendOfflineFunction implements PPFunction{

    @Resource
    ClientTabPaneController clientTabPaneController;

    private static final PPProtocolEnum protocol = PPProtocolEnum.FRIEND_OFFLINE;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        PPClientContext.friends.forEach(friend -> {
            if (friend.getId().equals(o)){
                friend.setOnline(false);
                Platform.runLater(()-> clientTabPaneController.friendList.refresh());
            }
        });
        return ChannelContext.release();
    }
}
