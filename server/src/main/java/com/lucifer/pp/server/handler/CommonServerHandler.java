package com.lucifer.pp.server.handler;

import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class CommonServerHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    ServerFunctionHandler functionHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        String ip = channelHandlerContext.channel().remoteAddress().toString();
        UserContext.setIP(ip.substring(1,ip.indexOf(":")));
        log.info(ip+" 链接进入");
        ChannelContext.setChannel(channelHandlerContext);
        PPProtocol ppProtocol = JSONUtil.toBean(s, PPProtocol.class);
        functionHandler.execute(ppProtocol.getPpProtocol(), ppProtocol.getData());
    }
}
