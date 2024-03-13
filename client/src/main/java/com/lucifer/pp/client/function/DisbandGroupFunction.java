package com.lucifer.pp.client.function;

import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.gui.controller.ClientTabPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DisbandGroupFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.DISBAND_GROUP;
    private final ClientTabPaneController clientTabPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        Long gid = Long.valueOf(String.valueOf(o));
        Optional<Group> groupOptional = PPClientContext.groups.stream()
                .filter(group -> group.getId().equals(gid)).findFirst();
        groupOptional.ifPresent(group -> {
            PPClientContext.groups.remove(group);
            Platform.runLater(() -> clientTabPaneController.groupList.refresh());
        });
        return ChannelContext.release();
    }
}
