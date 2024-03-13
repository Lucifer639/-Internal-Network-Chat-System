package com.lucifer.pp.server.function;

import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.entity.pp.PPFriend;
import com.lucifer.pp.common.security.TokenUtil;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class LogoutFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.LOGOUT;
    @Resource
    PPFriendService friendService;
    @Resource
    RedisUtil redisUtil;
    @Resource
    NetUtil netUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    public Object apply(Object o) {
        Long uid = UserContext.getUID();
        redisUtil.removeOnlineUser(uid);
        List<PPFriend> friends = friendService.findFriends(uid);
        friends.forEach(friend->{
            if (friend.getUserIdA().equals(uid)){
                if (redisUtil.isOnline(friend.getUserIdB())){
                    HeartBeatContext context = (HeartBeatContext) redisUtil.getHashValue(BaseConstant.REDIS_ONLINE_USER_KEY,
                            String.valueOf(friend.getUserIdB()));
                    PPProtocol<Long> ppProtocol = PPProtocol.of(PPProtocolEnum.FRIEND_OFFLINE,uid);
                    netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
                }
            }else if (friend.getUserIdB().equals(uid)){
                if (redisUtil.isOnline(friend.getUserIdA())){
                    HeartBeatContext context = (HeartBeatContext) redisUtil.getHashValue(BaseConstant.REDIS_ONLINE_USER_KEY,
                            String.valueOf(friend.getUserIdA()));
                    PPProtocol<Long> ppProtocol = PPProtocol.of(PPProtocolEnum.FRIEND_OFFLINE,uid);
                    netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
                }
            }
        });
        return ChannelContext.release();
    }
}
