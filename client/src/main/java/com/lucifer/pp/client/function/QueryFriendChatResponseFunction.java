package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.common.dto.FriendChat;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.controller.ChatPaneController;
import com.lucifer.pp.net.data.QueryFriendChatResponseData;
import com.lucifer.pp.net.netenum.ChatEnum;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class QueryFriendChatResponseFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUERY_FRIEND_CHAT_RESPONSE;
    private final ApplicationContext applicationContext;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        QueryFriendChatResponseData data = ((JSONObject) o).toBean(QueryFriendChatResponseData.class);
        ChatPaneController chatPaneController = applicationContext.getBean(ChatPaneController.class);
        PageInfo<?> pageInfo = data.getPageInfo();
        Stream<FriendChat> sorted = pageInfo.getList().stream()
                .map(item -> ((JSONObject) item).toBean(FriendChat.class))
                .sorted(Comparator.comparing(FriendChat::getTime));
        AtomicInteger index = new AtomicInteger();
        AtomicLong currentTime = new AtomicLong();
        sorted.forEach(friendChat -> Platform.runLater(() -> {
            if (Math.abs(friendChat.getTime() - currentTime.get()) > GUIConstant.TIME_LABEL_INTERVAL){
                chatPaneController.addTimeLabel(data.getFriendId(),friendChat.getTime(),index.get());
                currentTime.set(index.longValue());
                index.getAndIncrement();
            }
            if (friendChat.getSender().equals(PPClientContext.uid)){
                chatPaneController.insertChatBubble(false,null ,friendChat.getContent(), index.get());
            }else{
                String avatar = null;
                Optional<Friend> optionalFriend = PPClientContext.friends.stream()
                        .filter(friend -> friend.getId().equals(friendChat.getSender()))
                        .findFirst();
                if (optionalFriend.isPresent()){
                    avatar = optionalFriend.get().getAvatar();
                }
                chatPaneController.insertChatBubble(true,avatar,friendChat.getContent(), index.get());
            }
            index.getAndIncrement();
        }));
        PPClientContext.friendChatPages.put(data.getFriendId(),pageInfo);
        if (pageInfo.isHasNextPage()){
            Platform.runLater(()->{
                chatPaneController.addHistoryLabel(data.getFriendId(),ChatEnum.FRIEND);
            });
        }
        return null;
    }
}
