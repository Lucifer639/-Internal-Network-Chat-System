package com.lucifer.pp.client.util;

import cn.hutool.json.JSONUtil;
import com.lucifer.pp.client.handler.CommonClientHandler;
import com.lucifer.pp.common.properties.ServerProperties;
import com.lucifer.pp.net.codec.Base64Codec;
import com.lucifer.pp.net.codec.BaseCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class NetUtil {

    @Resource
    CommonClientHandler commonClientHandler;
    @Resource
    ServerProperties serverProperties;

    public CompletableFuture<String> sendMessage(Object msg){
        return sendMessage(serverProperties.getIp(), serverProperties.getPort(), JSONUtil.toJsonStr(msg));
    }

    public CompletableFuture<String> sendMessage(String ip, int port, Object msg){
        return CompletableFuture.supplyAsync(()->{
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(ip,port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new BaseCodec());
                            socketChannel.pipeline().addLast(new Base64Codec());
                            socketChannel.pipeline().addLast(commonClientHandler);
                        }
                    });
            try{
                ChannelFuture future = bootstrap.connect().sync();
                future.channel().writeAndFlush(msg);
                future.channel().closeFuture().sync();
                return "ok";
            }catch (Exception e){
                throw new RuntimeException(e);
            } finally {
                group.shutdownGracefully();
            }
        });
    }
}
