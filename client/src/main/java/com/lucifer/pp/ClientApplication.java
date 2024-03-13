package com.lucifer.pp;

import com.lucifer.pp.gui.view.LoginView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.CompletableFuture;

/**
 * @description 内网聊天系统客户端启动类/Internal Network Chat System Client Startup Class
 * @author wangpy
 * @date 2023/12/1
 * @version 0.1
 */

@SpringBootApplication
@MapperScan("com.lucifer.pp.common.mapper")
public class ClientApplication extends MyAbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        launch(ClientApplication.class, LoginView.class,args);
    }

}
