package com.lucifer.pp.client.handler;

import com.lucifer.pp.client.function.PPFunction;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClientFunctionHandler implements CommandLineRunner {

    private static final HashMap<PPProtocolEnum, PPFunction> functionMap = new HashMap<>();

    @Resource
    ApplicationContext applicationContext;

    public Object execute(PPProtocolEnum ppProtocolEnum,Object data){
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
