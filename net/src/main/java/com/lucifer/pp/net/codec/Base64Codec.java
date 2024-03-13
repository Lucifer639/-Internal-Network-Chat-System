package com.lucifer.pp.net.codec;

import com.lucifer.pp.common.security.AES;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class Base64Codec extends MessageToMessageCodec<String,String> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        list.add(AES.encryptByAES(s));
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        list.add(AES.decryptByAES(s));
    }
}
