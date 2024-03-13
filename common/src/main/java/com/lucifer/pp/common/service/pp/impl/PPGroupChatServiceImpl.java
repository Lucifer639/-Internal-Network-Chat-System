package com.lucifer.pp.common.service.pp.impl;

import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.dto.GroupChat;
import com.lucifer.pp.common.entity.pp.PPGroupChat;
import com.lucifer.pp.common.mapper.pp.PPGroupChatMapper;
import com.lucifer.pp.common.service.pp.PPGroupChatService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PPGroupChatServiceImpl extends BaseServiceImpl<PPGroupChatMapper, PPGroupChat> implements PPGroupChatService {

    @Resource
    PPGroupChatMapper groupChatMapper;

    @Override
    public List<GroupChat> queryGroupChat(Long groupId) {
        return groupChatMapper.queryGroupChat(groupId);
    }
}
