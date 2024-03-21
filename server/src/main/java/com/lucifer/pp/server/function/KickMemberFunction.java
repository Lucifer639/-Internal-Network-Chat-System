package com.lucifer.pp.server.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.annotation.Permission;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.DelGroupMemberData;
import com.lucifer.pp.net.data.KickMemberData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.SysRoleEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KickMemberFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.KICK_MEMBER;
    private final PPGroupMemberService groupMemberService;
    private final SysUserRoleService userRoleService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    @Permission(BaseConstant.DELETE_GROUP_MEMBER_CODE)
    @Transactional
    public Object apply(Object o) {
        KickMemberData data = ((JSONObject) o).toBean(KickMemberData.class);
        PPGroupMember executor = groupMemberService.queryPPGroupMember(data.getGroupId(),UserContext.getUID());
        PPGroupMember groupMember = groupMemberService.queryPPGroupMember(data.getGroupId(),data.getMemberId());
        if (ObjectUtil.isEmpty(executor) || ObjectUtil.isEmpty(groupMember)) return ChannelContext.release();
        if (groupMember.getLevel().equals(GroupMemberLevel.LEADER.level)) return ChannelContext.release();
        if (executor.getLevel().equals(GroupMemberLevel.MEMBER.level)) return ChannelContext.release();
        if (executor.getLevel().equals(GroupMemberLevel.MANAGER.level)
                && groupMember.getLevel().equals(GroupMemberLevel.MANAGER.level)) return ChannelContext.release();

        groupMemberService.doRemove(groupMember);
        DelGroupMemberData response = new DelGroupMemberData(data.getGroupId(),data.getMemberId());
        PPProtocol<DelGroupMemberData> ppProtocol = PPProtocol.of(PPProtocolEnum.DELETE_GROUP_MEMBER,response);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        ChannelContext.release();
        //被踢出者是群管理员，且被踢出后没有管理员身份，则收回权限
        QueryWrapper<PPGroupMember> queryWrapper = new QueryWrapper<>();
        if (groupMember.getLevel().equals(GroupMemberLevel.MANAGER.level)){
            queryWrapper.eq("user_id",data.getMemberId())
                    .eq("level",GroupMemberLevel.MANAGER.level);
            if (ObjectUtil.isEmpty(groupMemberService.getOne(queryWrapper))
                    && userRoleService.hasRole(data.getMemberId(),SysRoleEnum.GROUP_MANAGER.roleCode)){
                QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id",data.getMemberId())
                        .eq("role_id",SysRoleEnum.GROUP_MANAGER.id);
                SysUserRole one = userRoleService.getOne(wrapper);
                userRoleService.doRemove(one);
            }
        }

        queryWrapper.clear();
        queryWrapper.eq("group_id",data.getGroupId());
        List<PPGroupMember> list = groupMemberService.list(queryWrapper);
        list.forEach(ppGroupMember->{
            if (!ppGroupMember.getUserId().equals(UserContext.getUID()) && redisUtil.isOnline(ppGroupMember.getUserId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(ppGroupMember.getUserId());
                netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT,JSONUtil.toJsonStr(ppProtocol));
            }
        });
        return null;
    }
}
