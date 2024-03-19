package com.lucifer.pp.server;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.GroupChat;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.data.QueryGroupChatResponseData;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringBootTest
class ServerApplicationTests {

    @Resource
    RedisTemplate<String,Object> redisTemplate;
    @Resource
    SysUserRoleService userRoleService;

    @Test
    void contextLoads() {
        List<Integer> list = Arrays.asList(1,2,3,4,5);
        Integer integer = list.stream().filter(i -> i.equals(6)).findFirst().orElse(7);
        System.out.println(integer);
    }

}
