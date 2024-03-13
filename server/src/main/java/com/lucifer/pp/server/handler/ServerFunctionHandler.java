package com.lucifer.pp.server.handler;

import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.function.PPFunction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Component
@Slf4j
public class ServerFunctionHandler implements CommandLineRunner {

    private static final HashMap<PPProtocolEnum, Function<Object,Object>> functionMap = new HashMap<>();

    @Autowired
    ApplicationContext applicationContext;


    public Object execute(PPProtocolEnum ppProtocolEnum,Object data){
        if (!functionMap.containsKey(ppProtocolEnum)){
            log.warn(UserContext.getIP()+" 调用未知服务!");
            return ChannelContext.release();
        }
        log.info(UserContext.getIP()+" 调用服务 "+ppProtocolEnum.name());
        return functionMap.get(ppProtocolEnum).apply(data);
    }

    @Override
    public void run(String... args) {
        Map<String, PPFunction> beans = applicationContext.getBeansOfType(PPFunction.class);
        beans.forEach((name,instance)->{
            functionMap.put(instance.getProtocol(),instance);
        });
    }
}
