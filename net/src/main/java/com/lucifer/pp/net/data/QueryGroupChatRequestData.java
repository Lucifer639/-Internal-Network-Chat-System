package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryGroupChatRequestData {
    private String token;
    private Long groupId;
    private Integer limit;
    private Integer page;
}
