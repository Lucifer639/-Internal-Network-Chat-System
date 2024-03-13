package com.lucifer.pp.server.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.entity.pp.PPFriend;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.DelFriendData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DelFriendFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.DELETE_FRIEND;
    private final PPFriendService friendService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @Transactional
    @CheckLogin
    public Object apply(Object o) {
        DelFriendData data = ((JSONObject) o).toBean(DelFriendData.class);
        Long from = UserContext.getUID();
        assert from != null;
        QueryWrapper<PPFriend> queryWrapper = new QueryWrapper<>();
        if (from < data.getTo()){
            queryWrapper.eq("user_id_a",from)
                    .eq("user_id_b",data.getTo());
        }else{
            queryWrapper.eq("user_id_a",data.getTo())
                    .eq("user_id_b",from);
        }
        friendService.remove(queryWrapper);
        if (redisUtil.isOnline(data.getTo())){
            HeartBeatContext heartBeat = (HeartBeatContext) redisUtil.getHashValue(BaseConstant.REDIS_ONLINE_USER_KEY,
                    String.valueOf(data.getTo()));
            PPProtocol<Long> ppProtocol = PPProtocol.of(PPProtocolEnum.DELETE_FRIEND,UserContext.getUID());
            netUtil.sendMessage(heartBeat.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
        }
        Objects.requireNonNull(ChannelContext.getChannel()).close();
        ChannelContext.remove();
        return null;
    }
}
