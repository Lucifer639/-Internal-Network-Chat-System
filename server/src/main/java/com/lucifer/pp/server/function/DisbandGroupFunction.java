package com.lucifer.pp.server.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysRole;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.common.service.sys.SysRoleService;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.annotation.Permission;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.DisbandGroupData;
import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.MessageGenerator;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DisbandGroupFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.DISBAND_GROUP;
    private final PPGroupService groupService;
    private final PPGroupMemberService groupMemberService;
    private final SysUserRoleService userRoleService;
    private final SysRoleService roleService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    @Permission(BaseConstant.DISBAND_GROUP_CODE)
    @Transactional
    public Object apply(Object o) {
        DisbandGroupData data = ((JSONObject) o).toBean(DisbandGroupData.class);
        QueryWrapper<PPGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_id",data.getGid())
                .eq("user_id", UserContext.getUID())
                .eq("level", GroupMemberLevel.LEADER.level);
        PPGroupMember one = groupMemberService.getOne(memberWrapper);
        if (ObjectUtil.isEmpty(one)){
            PPProtocol<PPMessage> ppProtocol = MessageGenerator.generate(PPProtocolEnum.DISBAND_GROUP, StatusEnum.ERROR, "权限不足");
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(ppProtocol);
            return ChannelContext.release();
        }
        ChannelContext.release();
        memberWrapper.clear();
        memberWrapper.eq("group_id",data.getGid());
        List<PPGroupMember> members = groupMemberService.list(memberWrapper);
        members.forEach(member -> {
            if (redisUtil.isOnline(member.getUserId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(member.getUserId());
                PPProtocol<Long> ppProtocol = PPProtocol.of(PPProtocolEnum.DISBAND_GROUP,data.getGid());
                netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
            }
        });
        groupMemberService.remove(memberWrapper);
        groupService.removeById(data.getGid());
        memberWrapper.clear();
        memberWrapper.eq("user_id",UserContext.getUID())
                .eq("level", GroupMemberLevel.LEADER.level);
        //该用户没有群主身份
        if (ObjectUtil.isEmpty(groupMemberService.getOne(memberWrapper))){
            QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
            SysRole groupLeader = roleService.findByRoleCode(BaseConstant.GROUP_LEADER);
            wrapper.eq("user_id",UserContext.getUID())
                    .eq("role_id",groupLeader.getId());
            userRoleService.remove(wrapper);
        }
        return null;
    }
}
