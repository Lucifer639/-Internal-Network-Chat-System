package com.lucifer.pp.common.service.pp;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroupMember;

import java.util.List;

public interface PPGroupMemberService extends BaseService<PPGroupMember> {
    List<GroupMember> queryMemberByGroupId(Long groupId);
    boolean isInGroup(Long uid,Long groupId);
}
