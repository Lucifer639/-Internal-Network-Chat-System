package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryFriendChatRequestData {
    private String token;
    private Long friendId;
    private Integer page;
    private Integer limit;
}
