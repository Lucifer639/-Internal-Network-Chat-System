package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.GroupChat;
import com.lucifer.pp.gui.controller.ChatPaneController;
import com.lucifer.pp.net.data.QueryGroupChatResponseData;
import com.lucifer.pp.net.netenum.ChatEnum;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class QueryGroupChatResponseFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUERY_GROUP_CHAT_RESPONSE;
    private final ChatPaneController chatPaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        QueryGroupChatResponseData data = ((JSONObject) o).toBean(QueryGroupChatResponseData.class);
        PageInfo<?> pageInfo = data.getPageInfo();
        PPClientContext.groupChatPages.put(data.getGroupId(),pageInfo);
        Stream<GroupChat> sortedGroupChat = pageInfo.getList().stream()
                .map(item -> ((JSONObject) item).toBean(GroupChat.class))
                .sorted(Comparator.comparing(GroupChat::getTime));
        AtomicInteger index = new AtomicInteger();
        Platform.runLater(()-> sortedGroupChat.forEach(groupChat -> {
            if (groupChat.getSender().equals(PPClientContext.uid)){
                chatPaneController.insertChatBubble(false,null,groupChat.getContent(),index.get());
            }else{
                chatPaneController.insertChatBubble(true,groupChat.getAvatar(),groupChat.getContent(),index.get());
            }
            index.getAndIncrement();
        }));
        PPClientContext.groupChatPages.put(data.getGroupId(),data.getPageInfo());
        if (pageInfo.isHasNextPage()){
            Platform.runLater(()->{
                chatPaneController.addHistoryLabel(data.getGroupId(),ChatEnum.GROUP);
            });
        }
        return null;
    }
}
