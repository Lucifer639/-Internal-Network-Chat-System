package com.lucifer.pp.net.annotation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.service.sys.SysPermissionService;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final SysUserService userService;

    @Around("@annotation(com.lucifer.pp.net.annotation.Permission)")
    public Object handle(ProceedingJoinPoint pjp){
        if (ObjectUtil.isEmpty(UserContext.getToken())){
            return ChannelContext.release();
        }
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Permission permission = signature.getMethod().getAnnotation(Permission.class);
        String permissionCode = permission.value();
        List<String> permissionCodes = userService.getPermissionCode(UserContext.getUID());
        Object[] args = pjp.getArgs();
        JSONObject data = (JSONObject) args[0];
        if (!permissionCodes.contains(permissionCode) && !permissionCode.equals(BaseConstant.ALL_PERMISSION_CODE)){
            log.warn("用户id "+UserContext.getUID()+" 试图越权 "+permissionCode);
            PPMessage ppMessage = new PPMessage((PPProtocolEnum) data.get("ppProtocol"), StatusEnum.ERROR, "权限不足");
            PPProtocol<PPMessage> ppProtocol = new PPProtocol<>(PPProtocolEnum.MESSAGE,ppMessage);
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
            return ChannelContext.release();
        }
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return ChannelContext.release();
        }
    }

}
