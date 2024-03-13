package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.dto.GroupChat;
import com.lucifer.pp.common.service.pp.PPGroupChatService;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QueryGroupChatRequestData;
import com.lucifer.pp.net.data.QueryGroupChatResponseData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class QueryGroupChatFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUERY_GROUP_CHAT_REQUEST;
    private final PPGroupService groupService;
    private final PPGroupMemberService groupMemberService;
    private final PPGroupChatService groupChatService;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    public Object apply(Object o) {
        QueryGroupChatRequestData data = ((JSONObject) o).toBean(QueryGroupChatRequestData.class);
        if (!groupMemberService.isInGroup(UserContext.getUID(),data.getGroupId())) return ChannelContext.release();
        PageHelper.startPage(data.getPage(),data.getLimit());
        List<GroupChat> groupChats = groupChatService.queryGroupChat(data.getGroupId());
        PageInfo<GroupChat> pageInfo = new PageInfo<>(groupChats);
        QueryGroupChatResponseData response = new QueryGroupChatResponseData();
        response.setGroupId(data.getGroupId());
        response.setPageInfo(pageInfo);
        PPProtocol<QueryGroupChatResponseData> ppProtocol = PPProtocol.of(PPProtocolEnum.QUERY_GROUP_CHAT_RESPONSE,response);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
