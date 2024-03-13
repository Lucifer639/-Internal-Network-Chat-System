package com.lucifer.pp.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "pp.server")
@Component
public class ServerProperties {
    private String ip;
    private Integer port;
}
