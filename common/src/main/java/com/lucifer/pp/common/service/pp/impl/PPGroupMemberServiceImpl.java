package com.lucifer.pp.common.service.pp.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.mapper.pp.PPGroupMemberMapper;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PPGroupMemberServiceImpl extends BaseServiceImpl<PPGroupMemberMapper, PPGroupMember> implements PPGroupMemberService {

    @Resource
    private PPGroupMemberMapper groupMemberMapper;

    @Override
    public List<GroupMember> queryMemberByGroupId(Long groupId) {
        return groupMemberMapper.queryMemberByGroupId(groupId);
    }

    @Override
    public GroupMember queryMemberByGroupIdAndUID(Long groupId,Long uid){
        return groupMemberMapper.queryMemberByGroupIdAndUID(groupId,uid);
    }

    @Override
    public boolean isInGroup(Long uid, Long groupId) {
        return ObjectUtil.isNotEmpty(queryMemberByGroupIdAndUID(groupId,uid));
    }
}
