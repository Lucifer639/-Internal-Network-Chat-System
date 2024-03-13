package com.lucifer.pp.common.service.pp;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.dto.FriendChat;
import com.lucifer.pp.common.entity.pp.PPFriendChat;

import java.util.List;

public interface PPFriendChatService extends BaseService<PPFriendChat> {
    List<FriendChat> queryFriendChat(Long id,Long friendId);
}
