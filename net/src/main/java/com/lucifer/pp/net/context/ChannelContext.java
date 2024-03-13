package com.lucifer.pp.net.context;

import cn.hutool.core.util.ObjectUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

public class ChannelContext {
    private static final ThreadLocal<ChannelHandlerContext> channel = new ThreadLocal<>();

    public static ChannelHandlerContext getChannel(){
        if (ObjectUtil.isEmpty(channel.get())) return null;
        return channel.get();
    }

    public static void setChannel(ChannelHandlerContext chc){
        if (ObjectUtil.isNotEmpty(getChannel())){
            channel.remove();
        }
        channel.set(chc);
    }

    public static void remove(){
        channel.remove();
    }

    public static Object release(){
        Objects.requireNonNull(getChannel()).close();
        ChannelContext.remove();
        return null;
    }
}
