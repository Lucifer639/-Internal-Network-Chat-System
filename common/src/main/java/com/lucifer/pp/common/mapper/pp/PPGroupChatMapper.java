package com.lucifer.pp.common.mapper.pp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.dto.GroupChat;
import com.lucifer.pp.common.entity.pp.PPGroupChat;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PPGroupChatMapper extends BaseMapper<PPGroupChat> {
    List<GroupChat> queryGroupChat(Long groupId);
}
