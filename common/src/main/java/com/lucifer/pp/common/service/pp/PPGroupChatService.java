package com.lucifer.pp.common.service.pp;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.dto.GroupChat;
import com.lucifer.pp.common.entity.pp.PPGroupChat;

import java.util.List;

public interface PPGroupChatService extends BaseService<PPGroupChat> {
    List<GroupChat> queryGroupChat(Long groupId);
}
