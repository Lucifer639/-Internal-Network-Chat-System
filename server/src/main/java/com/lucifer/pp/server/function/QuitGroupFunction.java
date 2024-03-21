package com.lucifer.pp.server.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysRole;
import com.lucifer.pp.common.entity.sys.SysUserRole;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.sys.SysRoleService;
import com.lucifer.pp.common.service.sys.SysUserRoleService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.annotation.Permission;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.DelGroupMemberData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QuitGroupData;
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

@Component
@RequiredArgsConstructor
public class QuitGroupFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUIT_GROUP;
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
    @Transactional
    public Object apply(Object o) {
        ChannelContext.release();
        QuitGroupData data = ((JSONObject) o).toBean(QuitGroupData.class);
        QueryWrapper<PPGroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id",data.getGid());
        List<PPGroupMember> members = groupMemberService.list(wrapper);
        wrapper.eq("user_id",UserContext.getUID());
        groupMemberService.remove(wrapper);
        members.forEach(member -> {
            if (redisUtil.isOnline(member.getUserId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(member.getUserId());
                DelGroupMemberData delData = new DelGroupMemberData(data.getGid(),UserContext.getUID());
                PPProtocol<DelGroupMemberData> ppProtocol = PPProtocol.of(PPProtocolEnum.DELETE_GROUP_MEMBER,delData);
                netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
            }
        });
        SysRole manager = roleService.findByRoleCode(SysRoleEnum.GROUP_MANAGER.roleCode);
        wrapper.clear();
        wrapper.eq("user_id",UserContext.getUID())
                .eq("level", GroupMemberLevel.MANAGER.level);
        //该用户没有群管理员身份
        if (ObjectUtil.isEmpty(groupMemberService.getOne(wrapper))){
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",UserContext.getUID())
                    .eq("role_id",manager.getId());
            userRoleService.remove(queryWrapper);
        }
        return null;
    }
}
