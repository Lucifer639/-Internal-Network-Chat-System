package com.lucifer.pp.server.function;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.base.ErrorMessage;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPApplicant;
import com.lucifer.pp.common.entity.pp.PPFriend;
import com.lucifer.pp.common.entity.pp.PPGroup;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.entity.sys.SysUserPassword;
import com.lucifer.pp.common.security.TokenUtil;
import com.lucifer.pp.common.service.pp.PPApplicantService;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.common.service.sys.SysUserPasswordService;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.LoginRequestData;
import com.lucifer.pp.net.data.LoginResponseData;
import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.ApplyType;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.pojo.LockUser;
import com.lucifer.pp.server.util.MessageGenerator;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoginRequestFunction implements PPFunction {

    private static final PPProtocolEnum protocol = PPProtocolEnum.LOGIN_REQUEST;
    private final SysUserService userService;
    private final SysUserPasswordService userPasswordService;
    private final PPFriendService friendService;
    private final PPGroupService groupService;
    private final PPGroupMemberService groupMemberService;
    private final PPApplicantService applicantService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    @Override
    @Transactional
    public Object apply(Object data) {
        LoginRequestData loginData = ((JSONObject) data).toBean(LoginRequestData.class);
        QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
        userQuery.eq("user_code",loginData.getUserCode());
        SysUser user = userService.getOne(userQuery);
        if (ObjectUtil.isEmpty(user)){
            PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.LOGIN_REQUEST, StatusEnum.ERROR, ErrorMessage.USER_NOT_EXIST);
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
            return ChannelContext.release();
        }

        if (redisUtil.isLocked(user.getId())){
            PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.LOGIN_REQUEST, StatusEnum.ERROR, ErrorMessage.USER_IS_LOCKED);
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
            return ChannelContext.release();
        }

        QueryWrapper<SysUserPassword> passwordQuery = new QueryWrapper<>();
        passwordQuery.eq("user_id",user.getId())
                .eq("password",loginData.getPassword());
        SysUserPassword userPassword = userPasswordService.getOne(passwordQuery);
        if (ObjectUtil.isEmpty(userPassword)){

            int errorCount;
            if (redisUtil.willBeLocked(user.getId())) {
                LockUser lockUser = redisUtil.getLockUser(user.getId());
                if (lockUser.getErrorCount() >= BaseConstant.TIMES_TO_LOCK - 1) {
                    redisUtil.lockUser(user.getId());
                    redisUtil.removeLockUser(user.getId());
                    PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.LOGIN_REQUEST,
                            StatusEnum.ERROR, ErrorMessage.USER_IS_LOCKED);
                    Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
                    return ChannelContext.release();
                } else {
                    errorCount = lockUser.getErrorCount() + 1;
                    redisUtil.setLockUser(user.getId(), errorCount);
                }
            }else{
                errorCount = 1;
                redisUtil.setLockUser(user.getId(),1);
            }

            PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.LOGIN_REQUEST,
                    StatusEnum.ERROR, ErrorMessage.USER_PASSWORD_ERROR+",还有"+(BaseConstant.TIMES_TO_LOCK-errorCount)+"次机会");
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
            return ChannelContext.release();
        }
        userPassword.setLastTime(System.currentTimeMillis());
        userPassword.setLastIp(UserContext.getIP());
        String token = TokenUtil.token(user.getId(),user.getUserCode(), userPassword.getPassword());
        UserContext.setToken(token);
        userPasswordService.doUpdate(userPassword);
        redisUtil.setHeartBeat(user.getId(),token,UserContext.getIP());
        LoginResponseData loginResponseData = new LoginResponseData();
        loginResponseData.setAvatar(user.getAvatar());
        loginResponseData.setToken(token);
        loginResponseData.setName(user.getName());
        loginResponseData.setUserCode(user.getUserCode());

        //查询好友
        List<PPFriend> ppFriends = friendService.findFriends(user.getId());
        loginResponseData.setFriends(new ArrayList<>());
        ppFriends.forEach((ppFriend -> {
            SysUser friend;
            if (ppFriend.getUserIdA().equals(user.getId())){
                friend = userService.getById(ppFriend.getUserIdB());
            }else{
                friend = userService.getById(ppFriend.getUserIdA());
            }

            //若登陆者的好友在线，则通知好友登陆者上线了
            boolean isOnline = redisUtil.isOnline(friend.getId());
            if (isOnline){
                PPProtocol<Long> ppProtocol = PPProtocol.of(PPProtocolEnum.FRIEND_ONLINE,user.getId());
                String ip = ((HeartBeatContext) redisUtil.getHashValue(BaseConstant.REDIS_ONLINE_USER_KEY,
                        String.valueOf(friend.getId()))).getIp();
                netUtil.sendMessage(ip,BaseConstant.CLIENT_PORT,JSONUtil.toJsonStr(ppProtocol));
            }

            Friend friendDto = Friend.generate(friend);
            friendDto.setOnline(isOnline);
            loginResponseData.getFriends().add(friendDto);
        }));

        //查群
        QueryWrapper<PPGroupMember> groupQuery = new QueryWrapper<>();
        groupQuery.eq("user_id",user.getId());
        // 查询自己加入了哪些群
        List<PPGroupMember> ppGroupMembers = groupMemberService.list(groupQuery);
        loginResponseData.setGroups(new ArrayList<>());
        loginResponseData.setApplications(new ArrayList<>());
        ppGroupMembers.forEach((ppGroupMember -> {
            PPGroup ppGroup = groupService.getById(ppGroupMember.getGroupId());
            List<GroupMember> members = groupMemberService.queryMemberByGroupId(ppGroupMember.getGroupId());
            Group group = Group.generate(ppGroup);
            group.setMembers(members);
            loginResponseData.getGroups().add(group);
        }));

        PPProtocol<LoginResponseData> protocol = PPProtocol.of(PPProtocolEnum.LOGIN_RESPONSE, loginResponseData);
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(protocol));
        return ChannelContext.release();
    }

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }
}
