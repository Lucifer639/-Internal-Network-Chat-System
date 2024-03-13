package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.controller.ChatPaneController;
import com.lucifer.pp.gui.controller.ClientPaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.FriendChatData;
import com.lucifer.pp.net.netenum.ChatEnum;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FriendChatFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.FRIEND_CHAT;

    private final ChatPaneController chatPaneController;
    private final ClientPaneController clientPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        ChannelContext.release();
        FriendChatData data = ((JSONObject) o).toBean(FriendChatData.class);
        Optional<Friend> optionalFriend = PPClientContext.friends.stream()
                .filter(friend -> friend.getId().equals(data.getFrom()))
                .findFirst();

        optionalFriend.ifPresent(friend -> {
            Optional<Object[]> first = PPClientContext.newChats.stream()
                    .filter(newChat -> newChat[0].equals(friend.getId()) && newChat[1].equals(ChatEnum.FRIEND.code)).findFirst();
            Object[] chat = first.orElse(new Object[]{friend.getId(), ChatEnum.FRIEND.code});
            String avatar = friend.getAvatar();
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
