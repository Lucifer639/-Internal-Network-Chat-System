package com.lucifer.pp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendChat {
    private Long sender;
    private Long receiver;
    private String content;
    private Long time;
}
