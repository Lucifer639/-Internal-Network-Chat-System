package com.lucifer.pp.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lucifer.pp.common.base.BaseConstant;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @desc   使用token验证用户是否登录
 * @author zm
 **/
public class TokenUtil {
    //设置过期时间
    private static final long EXPIRE_DATE = 24 * 3600 * 1000;
    //token秘钥
    private static final String TOKEN_SECRET = "Lucifer";

    public static String token (Long uid,String userCode,String password){

        String token = "";
        try {
            //过期时间
            Date date = new Date(System.currentTimeMillis()+BaseConstant.TOKEN_EXPIRE);
            //秘钥及加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            //设置头部信息
            Map<String,Object> header = new HashMap<>();
            header.put("typ","JWT");
            header.put("alg","HS256");
            //携带userCode，password信息，生成签名
            token = JWT.create()
                    .withHeader(header)
                    .withClaim("uid",uid)
                    .withClaim("userCode",userCode)
                    .withClaim("password",password).withExpiresAt(date)
                    .sign(algorithm);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
        return token;
    }

    public static boolean verify(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        }catch (Exception e){
            return  false;
        }
    }

    public static Long getUID(String token) {
        return JWT.decode(token).getClaim("uid").asLong();
    }

    public static String getUserCode(String token){
        return JWT.decode(token).getClaim("userCode").asString();
    }

    public static String getPassword(String token){
        return JWT.decode(token).getClaim("password").asString();
    }

    //获取token过期时间，单位：毫秒
    public static long getExpireTime(String token){
        return JWT.decode(token).getExpiresAt().getTime();
    }
}