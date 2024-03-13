package com.lucifer.pp.net.data;

import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.dto.GroupChat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryGroupChatResponseData {
    private Long groupId;
    private PageInfo<GroupChat> pageInfo;
}
