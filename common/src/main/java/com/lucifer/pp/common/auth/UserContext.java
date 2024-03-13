package com.lucifer.pp.common.auth;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.common.security.TokenUtil;

/**
 * 用户登录信息上下文
 */
public class UserContext {

    // token
    private static final ThreadLocal<String> threadLocalToken = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalIP = new ThreadLocal<>();

    public static String getToken(){
        if (ObjectUtil.isEmpty(threadLocalToken)) return null;
        return threadLocalToken.get();
    }

    public static void setToken(String token){
        if (ObjectUtil.isNotEmpty(getToken())){
            threadLocalToken.remove();
        }
        threadLocalToken.set(token);
    }

    public static String getIP(){
        if (ObjectUtil.isEmpty(threadLocalIP)) return null;
        return threadLocalIP.get();
    }

    public static void setIP(String ip){
        if (ObjectUtil.isNotEmpty(threadLocalIP)){
            threadLocalIP.remove();
        }
        threadLocalIP.set(ip);
    }

    public static Long getUID(){
        if (ObjectUtil.isNotEmpty(getToken())){
            return TokenUtil.getUID(threadLocalToken.get());
        }
        return null;
    }

    public static String getUserCode(){
        if (ObjectUtil.isNotEmpty(getToken())){
            return TokenUtil.getUserCode(threadLocalToken.get());
        }
        return null;
    }

}
