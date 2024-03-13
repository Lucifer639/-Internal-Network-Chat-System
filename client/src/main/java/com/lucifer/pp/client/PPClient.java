package com.lucifer.pp.client;

import cn.hutool.json.JSONUtil;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.properties.ServerProperties;
import com.lucifer.pp.net.codec.Base64Codec;
import com.lucifer.pp.net.codec.BaseCodec;
import com.lucifer.pp.client.handler.CommonClientHandler;
import com.lucifer.pp.net.data.HeartBeatData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

@Component
public class PPClient {

    @Resource
    CommonClientHandler commonClientHandler;

    public final EventLoopGroup commonBossGroup = new NioEventLoopGroup();
    public final EventLoopGroup commonWorkerGroup = new NioEventLoopGroup();
    public final ServerBootstrap commonStrap = new ServerBootstrap();
    public final ExecutorService executors = Executors.newFixedThreadPool(1);
    public ChannelFuture future;

    public PPClient(){
        initClient();
    }

    void initClient(){
        executors.execute(()->{
            try {
                commonStrap.group(commonBossGroup,commonWorkerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG,128)
                        .localAddress(BaseConstant.CLIENT_PORT)
                        .childOption(ChannelOption.SO_KEEPALIVE,true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline().addLast(new BaseCodec());
                                socketChannel.pipeline().addLast(new Base64Codec());
                                socketChannel.pipeline().addLast(commonClientHandler);
                            }
                        });
                future = commonStrap.bind().sync();
                future.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                commonBossGroup.shutdownGracefully();
                commonWorkerGroup.shutdownGracefully();
                executors.shutdown();
            }
        });
    }
}
