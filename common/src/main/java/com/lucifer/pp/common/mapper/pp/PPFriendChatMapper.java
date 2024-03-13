package com.lucifer.pp.common.mapper.pp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.dto.FriendChat;
import com.lucifer.pp.common.entity.pp.PPFriendChat;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PPFriendChatMapper extends BaseMapper<PPFriendChat> {
    List<FriendChat> queryFriendChat(Long uid, Long friendId);
}
