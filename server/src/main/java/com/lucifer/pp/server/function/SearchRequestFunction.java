package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.entity.pp.PPGroup;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.SearchRequestData;
import com.lucifer.pp.net.data.SearchResponseData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.SearchType;
import com.lucifer.pp.server.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class SearchRequestFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.SEARCH_REQUEST;

    @Resource
    SysUserService userService;
    @Resource
    PPGroupService groupService;
    @Resource
    PPGroupMemberService groupMemberService;
    @Resource
    RedisUtil redisUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    public Object apply(Object o) {
        SearchRequestData data = ((JSONObject) o).toBean(SearchRequestData.class);
        PPProtocol<List<SearchResponseData>> ppProtocol = new PPProtocol<>();
        ppProtocol.setPpProtocol(PPProtocolEnum.SEARCH_RESPONSE);
        ppProtocol.setData(new ArrayList<>());
        if (data.getSearchType() == SearchType.FRIEND){
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_code",data.getSearchWord())
                    .or()
                    .like("name",data.getSearchWord());
            List<SysUser> users = userService.list(queryWrapper);
            users.forEach(user -> {
                SearchResponseData searchResponseData = new SearchResponseData();
                searchResponseData.setId(user.getId());
                searchResponseData.setAvatar(user.getAvatar());
                searchResponseData.setName(user.getName());
                searchResponseData.setUserCode(user.getUserCode());
                searchResponseData.setOnline(redisUtil.isOnline(user.getId()));
                ppProtocol.getData().add(searchResponseData);
            });
        }else if (data.getSearchType() == SearchType.GROUP){
            QueryWrapper<PPGroup> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",data.getSearchWord())
                    .or()
                    .like("name",data.getSearchWord());
            List<PPGroup> groups = groupService.list(queryWrapper);
            groups.forEach(group->{
                SearchResponseData searchResponseData = new SearchResponseData();
                searchResponseData.setId(group.getId());
                searchResponseData.setAvatar(group.getAvatar());
                searchResponseData.setName(group.getName());

                QueryWrapper<PPGroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
                groupMemberQueryWrapper.eq("group_id",group.getId())
                        .eq("level",0);
                PPGroupMember one = groupMemberService.getOne(groupMemberQueryWrapper);
                SysUser manager = userService.getById(one.getUserId());
                searchResponseData.setUserCode(manager.getUserCode());
                ppProtocol.getData().add(searchResponseData);
            });
        }
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
        return ChannelContext.release();
    }
}
