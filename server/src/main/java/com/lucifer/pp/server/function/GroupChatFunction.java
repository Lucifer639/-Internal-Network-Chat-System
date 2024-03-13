package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPGroupChat;
import com.lucifer.pp.common.service.pp.PPGroupChatService;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.GroupChatData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupChatFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.GROUP_CHAT;
    private final PPGroupService groupService;
    private final PPGroupChatService groupChatService;
    private final PPGroupMemberService groupMemberService;
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
        GroupChatData data = ((JSONObject) o).toBean(GroupChatData.class);
        if (!groupMemberService.isInGroup(UserContext.getUID(),data.getGroupId())) return null;

        PPGroupChat groupChat = new PPGroupChat(UserContext.getUID(),data.getGroupId(),data.getContent());
        groupChatService.doAdd(groupChat);
        List<GroupMember> groupMembers = groupMemberService.queryMemberByGroupId(data.getGroupId());
        data.setToken(null);
        data.setSender(UserContext.getUID());
        PPProtocol<GroupChatData> ppProtocol = PPProtocol.of(PPProtocolEnum.GROUP_CHAT,data);
        groupMembers.forEach(groupMember -> {
            if (!groupMember.getId().equals(UserContext.getUID()) && redisUtil.isOnline(groupMember.getId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(groupMember.getId());
                netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
            }
        });
        return null;
    }
}
