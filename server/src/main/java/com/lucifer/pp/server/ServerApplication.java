package com.lucifer.pp.server;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @description 内网聊天系统服务端启动类/Internal Network Chat System Server Startup Class
 * @author wangpy
 * @date 2023/12/1
 * @version 0.1
 */

@SpringBootApplication
@MapperScan("com.lucifer.pp.common.mapper")
@ComponentScan({"com.lucifer.pp.common","com.lucifer.pp.net","com.lucifer.pp.server"})
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
