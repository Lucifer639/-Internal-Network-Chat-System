package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.dto.FriendChat;
import com.lucifer.pp.common.service.pp.PPFriendChatService;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QueryFriendChatRequestData;
import com.lucifer.pp.net.data.QueryFriendChatResponseData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class QueryFriendChatFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUERY_FRIEND_CHAT_REQUEST;
    private final PPFriendService friendService;
    private final PPFriendChatService friendChatService;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    public Object apply(Object o) {
        QueryFriendChatRequestData data = ((JSONObject) o).toBean(QueryFriendChatRequestData.class);
        if (!friendService.isFriend(UserContext.getUID(),data.getFriendId())) return ChannelContext.release();
        PageHelper.startPage(data.getPage(),data.getLimit());
        List<FriendChat> friendChats = friendChatService.queryFriendChat(UserContext.getUID(), data.getFriendId());
        PageInfo<FriendChat> pageInfo = new PageInfo<>(friendChats);
        QueryFriendChatResponseData response = new QueryFriendChatResponseData();
        response.setFriendId(data.getFriendId());
        response.setPageInfo(pageInfo);
        PPProtocol<QueryFriendChatResponseData> ppProtocol = PPProtocol.of(PPProtocolEnum.QUERY_FRIEND_CHAT_RESPONSE,response);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
