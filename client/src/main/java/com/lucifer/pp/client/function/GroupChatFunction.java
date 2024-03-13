package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.controller.ChatPaneController;
import com.lucifer.pp.gui.controller.ClientPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.GroupChatData;
import com.lucifer.pp.net.netenum.ChatEnum;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GroupChatFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.GROUP_CHAT;
    private final ChatPaneController chatPaneController;
    private final ClientPaneController clientPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        ChannelContext.release();
        GroupChatData data = ((JSONObject) o).toBean(GroupChatData.class);
        Optional<Group> optionalGroup = PPClientContext.groups.stream()
                .filter(group -> group.getId().equals(data.getGroupId()))
                .findFirst();
        optionalGroup.ifPresent(group -> {
            Optional<Object[]> first = PPClientContext.newChats.stream()
                    .filter(chat -> chat[0].equals(group.getId()) && chat[1].equals(ChatEnum.GROUP.code))
                    .findFirst();
            Optional<GroupMember> optionalGroupMember = group.getMembers().stream()
                    .filter(groupMember -> groupMember.getId().equals(data.getSender())).findFirst();
            Object[] chat = first.orElse(new Object[]{group.getId(),ChatEnum.GROUP.code});
            first.ifPresentOrElse(newChat -> {}, ()->{
                PPClientContext.newChats.add(chat);
            });
            Platform.runLater(()->{
                if (!PPClientContext.chatWindowExist){
                    ImageView imageView = GUIConstant.CHAT_NEW_IMAGE;
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    clientPaneController.chatLabel.setGraphic(imageView);
                }else{
                    if (chatPaneController.chatPaneMap.containsKey((Long) chat[0])){
                        String avatar = null;
                        if (optionalGroupMember.isPresent()){
                            avatar = optionalGroupMember.get().getAvatar();
                        }
                        chatPaneController.insertChatBubble(true,avatar,data.getContent());
                    }else{
                        chatPaneController.addChat(chat);
                    }
                }
            });
        });
        return null;
    }
}
