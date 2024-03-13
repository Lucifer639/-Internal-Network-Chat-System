package com.lucifer.pp.server.function;

import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class HeartBeatFunction implements PPFunction {

    @Resource
    RedisUtil redisUtil;

    private static final PPProtocolEnum protocol = PPProtocolEnum.HEART_BEAT;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    public Object apply(Object o) {
        redisUtil.setHeartBeat(UserContext.getUID(),UserContext.getToken(),UserContext.getIP());
        Objects.requireNonNull(ChannelContext.getChannel()).close();
        ChannelContext.remove();
        return null;
    }
}
