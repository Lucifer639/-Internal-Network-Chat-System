package com.lucifer.pp.net.codec;

import com.lucifer.pp.net.context.ChannelContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseCodec extends ByteToMessageCodec<String> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String s, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        list.add(byteBuf.toString(CharsetUtil.UTF_8));
        byteBuf.skipBytes(byteBuf.readableBytes());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
