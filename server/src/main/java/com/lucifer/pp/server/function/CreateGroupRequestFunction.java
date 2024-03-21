package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroup;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysRole;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.common.service.sys.SysRoleService;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.CreateGroupRequestData;
import com.lucifer.pp.net.data.CreateGroupResponseData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.SysRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CreateGroupRequestFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.CREATE_GROUP_REQUEST;
    private final PPGroupService groupService;
    private final SysUserRoleService userRoleService;
    private final SysRoleService roleService;
    private final PPGroupMemberService groupMemberService;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    @Transactional
    public Object apply(Object o) {
        CreateGroupRequestData data = ((JSONObject) o).toBean(CreateGroupRequestData.class);
        boolean isGroupLeader = userRoleService.hasRole(UserContext.getUID(), SysRoleEnum.GROUP_LEADER.roleCode);
        PPGroup ppGroup = new PPGroup(data.getGroupName(),null);
        Long groupId = groupService.doAdd(ppGroup);
        PPGroupMember groupMember = new PPGroupMember(groupId,UserContext.getUID(), GroupMemberLevel.LEADER.level);
        groupMemberService.doAdd(groupMember);
        if (!isGroupLeader){
            SysRole role = roleService.findByRoleCode(SysRoleEnum.GROUP_LEADER.roleCode);
            SysUserRole userRole = new SysUserRole(UserContext.getUID(),role.getId());
            userRoleService.doAdd(userRole);
        }
        CreateGroupResponseData response = new CreateGroupResponseData(groupId,data.getGroupName());
        PPProtocol<CreateGroupResponseData> ppProtocol = new PPProtocol<>(PPProtocolEnum.CREATE_GROUP_RESPONSE,response);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
