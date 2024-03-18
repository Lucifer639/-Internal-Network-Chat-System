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
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.annotation.Permission;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.LevelUpdateMemberData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.UpdateMemberData;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LevelUpdateMemberFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.LEVEL_UPDATE_MEMBER;
    private final PPGroupMemberService groupMemberService;

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
        QueryWrapper<PPGroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id",data.getGroupId())
                .eq("user_id", UserContext.getUID());
        PPGroupMember leader = groupMemberService.getOne(queryWrapper);
        if (ObjectUtil.isEmpty(leader) || !leader.getLevel().equals(GroupMemberLevel.LEADER.level)
            || !groupMemberService.isInGroup(data.getMemberId(),data.getGroupId())) return ChannelContext.release();

        UpdateWrapper<PPGroupMember> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",data.getGroupId())
                .eq("user_id",data.getMemberId());
        if (data.getMemberLevel() == GroupMemberLevel.MANAGER){
            updateWrapper.set("level",GroupMemberLevel.MANAGER.level);
            groupMemberService.update(updateWrapper);
        }else if (data.getMemberLevel() == GroupMemberLevel.MEMBER){
            updateWrapper.set("level",GroupMemberLevel.MEMBER.level);
            groupMemberService.update(updateWrapper);
        }

        GroupMember groupMember = groupMemberService.queryMemberByGroupIdAndUID(data.getGroupId(),data.getMemberId());
        UpdateMemberData response = new UpdateMemberData(data.getGroupId(),groupMember);
        PPProtocol<UpdateMemberData> ppProtocol = PPProtocol.of(PPProtocolEnum.UPDATE_GROUP_MEMBER,response);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
