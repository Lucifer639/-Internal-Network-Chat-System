package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.entity.pp.PPApplicant;
import com.lucifer.pp.common.entity.pp.PPGroup;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.service.pp.PPApplicantService;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QueryApplyRequestData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class QueryApplyRequestFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUERY_APPLY_REQUEST;
    private final PPApplicantService applicantService;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    public Object apply(Object o) {
        QueryApplyRequestData data = ((JSONObject) o).toBean(QueryApplyRequestData.class);
        PageHelper.startPage(data.getPage(),data.getLimit());
        List<Application> applications = applicantService.queryApplication(UserContext.getUID());
        PageInfo<Application> pageInfo = new PageInfo<>(applications);
        PPProtocol<PageInfo<Application>> ppProtocol = PPProtocol.of(PPProtocolEnum.QUERY_APPLY_RESPONSE,pageInfo);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
