package com.lucifer.pp.client.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.client.handler.ClientFunctionHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class CommonClientHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    ClientFunctionHandler clientFunctionHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        ChannelContext.setChannel(channelHandlerContext);
        if (ObjectUtil.isNotEmpty(s)){
            PPProtocol ppProtocol = JSONUtil.toBean(s, PPProtocol.class);
            clientFunctionHandler.execute(ppProtocol.getPpProtocol(),ppProtocol.getData());
        }
    }
}
