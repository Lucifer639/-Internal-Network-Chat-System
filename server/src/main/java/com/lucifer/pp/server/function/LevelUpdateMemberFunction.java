package com.lucifer.pp.server.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysRole;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.sys.SysRoleService;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.annotation.Permission;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.LevelUpdateMemberData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.UpdateMemberData;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.SysRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LevelUpdateMemberFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.LEVEL_UPDATE_MEMBER;
    private final PPGroupMemberService groupMemberService;
    private final SysUserRoleService userRoleService;
    private final SysRoleService roleService;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    @Permission(BaseConstant.UPDATE_GROUP_MEMBER_CODE)
    @Transactional
    public Object apply(Object o) {
        LevelUpdateMemberData data = ((JSONObject) o).toBean(LevelUpdateMemberData.class);
        PPGroupMember leader = groupMemberService.queryPPGroupMember(data.getGroupId(),UserContext.getUID());
        GroupMember groupMember = groupMemberService.queryMemberByGroupIdAndUID(data.getGroupId(), data.getMemberId());
        if (ObjectUtil.isEmpty(leader) || !leader.getLevel().equals(GroupMemberLevel.LEADER.level)
            || ObjectUtil.isEmpty(groupMember)) return ChannelContext.release();


        PPGroupMember member = groupMemberService.queryPPGroupMember(data.getGroupId(),data.getMemberId());
        if (data.getMemberLevel() == GroupMemberLevel.MANAGER){
            member.setLevel(GroupMemberLevel.MANAGER.level);
            groupMember.setLevel(GroupMemberLevel.MANAGER.level);
            groupMember.setLevelDescription(GroupMemberLevel.MANAGER.levelDescription);
            groupMemberService.doUpdate(member);
            if (!userRoleService.hasRole(data.getMemberId(), SysRoleEnum.GROUP_MANAGER.roleCode)){
                userRoleService.addRole(data.getMemberId(),SysRoleEnum.GROUP_MANAGER.roleCode);
            }
        }else if (data.getMemberLevel() == GroupMemberLevel.MEMBER){
            member.setLevel(GroupMemberLevel.MEMBER.level);
            groupMember.setLevel(GroupMemberLevel.MEMBER.level);
            groupMember.setLevelDescription(GroupMemberLevel.MEMBER.levelDescription);
            groupMemberService.doUpdate(member);
            QueryWrapper<PPGroupMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",data.getMemberId())
                    .eq("level",GroupMemberLevel.MANAGER.level);
            //该用户没有管理员身份,则移除管理员权限
            if (ObjectUtil.isEmpty(groupMemberService.getOne(queryWrapper))){
                QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
                SysRole groupManager = roleService.findByRoleCode(SysRoleEnum.GROUP_MANAGER.roleCode);
                wrapper.eq("user_id",data.getMemberId())
                        .eq("role_id",groupManager.getId());
                SysUserRole one = userRoleService.getOne(wrapper);
                userRoleService.doRemove(one);
            }
        }

        UpdateMemberData response = new UpdateMemberData(data.getGroupId(),groupMember);
        PPProtocol<UpdateMemberData> ppProtocol = PPProtocol.of(PPProtocolEnum.UPDATE_GROUP_MEMBER,response);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
