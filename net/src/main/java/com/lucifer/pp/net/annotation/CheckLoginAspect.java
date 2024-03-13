package com.lucifer.pp.net.annotation;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.security.TokenUtil;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.HeartBeatData;
import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckLoginAspect {

    private final SysUserService userService;

    @Around("@annotation(com.lucifer.pp.net.annotation.CheckLogin)")
    public Object handle(ProceedingJoinPoint pjp){
        Object[] args = pjp.getArgs();
        JSONObject data = (JSONObject) args[0];
        if (!TokenUtil.verify((String) data.get("token"))){
            PPProtocol<String> ppProtocol = new PPProtocol<>();
            ppProtocol.setPpProtocol(PPProtocolEnum.RE_LOGIN);
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
            return ChannelContext.release();
        }
        Long uid = TokenUtil.getUID((String) data.get("token"));
        SysUser user = userService.getById(uid);
        if (user.getState().equals(0)){
            if (!data.containsKey("heartBeatFlag")){
                PPMessage ppMessage = new PPMessage((PPProtocolEnum) data.get("ppProtocol"), StatusEnum.ERROR, "你已被封禁");
                PPProtocol<PPMessage> ppProtocol = new PPProtocol<>(PPProtocolEnum.MESSAGE,ppMessage);
                Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
            }
            return ChannelContext.release();
        }
        UserContext.setToken((String) data.get("token"));
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return ChannelContext.release();
        }
    }
}
