package com.lucifer.pp.client.function;

import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.REFRESH_TOKEN;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        PPClientContext.token = (String) o;
        return ChannelContext.release();
    }
}
