package com.lucifer.pp.net.data;

import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.dto.FriendChat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryFriendChatResponseData {
    private Long friendId;
    private PageInfo<FriendChat> pageInfo;
}
