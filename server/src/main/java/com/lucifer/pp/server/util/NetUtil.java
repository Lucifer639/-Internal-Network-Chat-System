package com.lucifer.pp.server.util;

import com.lucifer.pp.net.codec.Base64Codec;
import com.lucifer.pp.net.codec.BaseCodec;
import com.lucifer.pp.server.PPServerContext;
import com.lucifer.pp.server.handler.CommonServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class NetUtil {

    @Resource
    CommonServerHandler commonServerHandler;

    public CompletableFuture<Void> sendMessage(String ip, int port, Object msg){
        return CompletableFuture.runAsync(()->{
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
                            socketChannel.pipeline().addLast(commonServerHandler);
                        }
                    });
            try{
                ChannelFuture future = bootstrap.connect().sync();
                future.channel().writeAndFlush(msg);
                future.channel().closeFuture().sync();
            }catch (Exception e){
                throw new RuntimeException(e);
            } finally {
                group.shutdownGracefully();
            }
        }, PPServerContext.netUtilThreadPool);
    }
}
