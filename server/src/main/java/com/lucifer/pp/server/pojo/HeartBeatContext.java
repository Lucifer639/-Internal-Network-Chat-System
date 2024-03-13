package com.lucifer.pp.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartBeatContext implements Serializable {
    private String token;
    private String ip;
    // 最后发送心跳包毫秒数
    private long lastTime;
}
