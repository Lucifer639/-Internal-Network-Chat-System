package com.lucifer.pp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChat {
    private Long sender;
    //发送者头像
    private String avatar;
    private Long groupId;
    private String content;
    private Long time;
}
