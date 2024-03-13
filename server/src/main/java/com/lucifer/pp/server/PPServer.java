package com.lucifer.pp.server;

import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.entity.pp.PPFriend;
import com.lucifer.pp.common.properties.ServerProperties;
import com.lucifer.pp.common.security.TokenUtil;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.net.codec.Base64Codec;
import com.lucifer.pp.net.codec.BaseCodec;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.handler.CommonServerHandler;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class PPServer {

    public final EventLoopGroup commonBossGroup = new NioEventLoopGroup();
    public final EventLoopGroup commonWorkerGroup = new NioEventLoopGroup(30);
    public final ServerBootstrap commonStrap = new ServerBootstrap();
    public final ExecutorService executors = Executors.newFixedThreadPool(1);
    public ChannelFuture future;

    @Resource
    private CommonServerHandler commonServerHandler;
    @Resource
    ServerProperties serverProperties;
    @Resource
    RedisUtil redisUtil;
    @Resource
    PPFriendService friendService;
    @Resource
    NetUtil netUtil;

    @PostConstruct
    void initServer() {
        executors.execute(() -> {
            commonStrap.group(commonBossGroup, commonWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .localAddress(serverProperties.getPort())
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new BaseCodec());
                            socketChannel.pipeline().addLast(new Base64Codec());
                            socketChannel.pipeline().addLast(commonServerHandler);
                        }
                    });
            try {
                future = commonStrap.bind().sync();
                log.info("服务器启动");
                future.channel().eventLoop().scheduleAtFixedRate(this::checkHeartBeat,0,
                        BaseConstant.HEART_BEAT_CHECK_TIME, TimeUnit.MILLISECONDS);
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

    private void checkHeartBeat(){
        List<Object> heartBeats = redisUtil.getAllHeartBeat();
        heartBeats.forEach(heartBeat->{
            HeartBeatContext context = (HeartBeatContext) heartBeat;
            long lastTime = context.getLastTime();
            long currentTime = System.currentTimeMillis();
            //用户断线
            if (currentTime - lastTime > BaseConstant.HEART_BEAT_CHECK_TIME){
                Long uid = TokenUtil.getUID(context.getToken());
                redisUtil.removeOnlineUser(uid);
                PPProtocol<Long> ppProtocol = new PPProtocol<>(PPProtocolEnum.FRIEND_OFFLINE,uid);
                List<PPFriend> friends = friendService.findFriends(uid);
                friends.forEach(friend->{
                    if (friend.getUserIdA().equals(uid) && redisUtil.isOnline(friend.getUserIdB())){
                        HeartBeatContext friendContext = (HeartBeatContext) redisUtil.
                                getHashValue(BaseConstant.REDIS_ONLINE_USER_KEY, String.valueOf(friend.getUserIdB()));
                        netUtil.sendMessage(friendContext.getIp(),BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
                    }else if (friend.getUserIdB().equals(uid) && redisUtil.isOnline(friend.getUserIdA())){
                        HeartBeatContext friendContext = (HeartBeatContext) redisUtil.
                                getHashValue(BaseConstant.REDIS_ONLINE_USER_KEY, String.valueOf(friend.getUserIdA()));
                        netUtil.sendMessage(friendContext.getIp(),BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
                    }
                });
            }else{
                long tokenExpireTime = TokenUtil.getExpireTime(context.getToken());
                //刷新token
                if (tokenExpireTime - currentTime < BaseConstant.REFRESH_TIME_BEFORE_TOKEN_EXPIRE){
                    Long uid = TokenUtil.getUID(context.getToken());
                    String userCode = TokenUtil.getUserCode(context.getToken());
                    String password = TokenUtil.getPassword(context.getToken());
                    String token = TokenUtil.token(uid, userCode, password);
                    PPProtocol<String> ppProtocol = new PPProtocol<>(PPProtocolEnum.REFRESH_TOKEN,token);
                    netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT,JSONUtil.toJsonStr(ppProtocol));
                }
            }
        });
    }
}
