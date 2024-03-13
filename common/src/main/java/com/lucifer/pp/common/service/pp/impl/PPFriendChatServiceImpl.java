package com.lucifer.pp.common.service.pp.impl;

import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.dto.FriendChat;
import com.lucifer.pp.common.entity.pp.PPFriendChat;
import com.lucifer.pp.common.mapper.pp.PPFriendChatMapper;
import com.lucifer.pp.common.service.pp.PPFriendChatService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PPFriendChatServiceImpl extends BaseServiceImpl<PPFriendChatMapper, PPFriendChat> implements PPFriendChatService {

    @Resource
    PPFriendChatMapper friendChatMapper;

    public List<FriendChat> queryFriendChat(Long id, Long friendId){
        return friendChatMapper.queryFriendChat(id,friendId);
    }
}
