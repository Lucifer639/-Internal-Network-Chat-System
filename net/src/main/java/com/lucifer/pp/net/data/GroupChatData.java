package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatData {
    private String token;
    private Long sender;
    private Long groupId;
    private String content;
}
