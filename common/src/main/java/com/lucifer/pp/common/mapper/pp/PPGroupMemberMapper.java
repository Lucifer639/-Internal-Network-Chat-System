package com.lucifer.pp.common.mapper.pp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface PPGroupMemberMapper extends BaseMapper<PPGroupMember> {
    List<GroupMember> queryMemberByGroupId(Long groupId);
    GroupMember queryMemberByGroupIdAndUID(Long groupId,Long uid);
}
