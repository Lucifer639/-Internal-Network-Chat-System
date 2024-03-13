package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.entity.pp.PPFriendChat;
import com.lucifer.pp.common.service.pp.PPFriendChatService;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.FriendChatData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FriendChatFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.FRIEND_CHAT;

    private final PPFriendService friendService;
    private final PPFriendChatService friendChatService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    @Transactional
    public Object apply(Object o) {
        ChannelContext.release();
        FriendChatData data = ((JSONObject) o).toBean(FriendChatData.class);
        if (!friendService.isFriend(UserContext.getUID(), data.getTo())) return null;
        PPFriendChat ppFriendChat = new PPFriendChat(UserContext.getUID(),data.getTo(),data.getContent());
        friendChatService.doAdd(ppFriendChat);
        if (redisUtil.isOnline(data.getTo())){
            data.setToken(null);
            HeartBeatContext heartBeat = redisUtil.getHeartBeatContext(data.getTo());
            PPProtocol<FriendChatData> ppProtocol = PPProtocol.of(PPProtocolEnum.FRIEND_CHAT,data);
            netUtil.sendMessage(heartBeat.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
        }
        return null;
    }
}
