package com.lucifer.pp.server.function;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.common.entity.pp.PPApplicant;
import com.lucifer.pp.common.entity.pp.PPFriend;
import com.lucifer.pp.common.entity.pp.PPGroup;
import com.lucifer.pp.common.entity.pp.PPGroupMember;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.service.pp.PPApplicantService;
import com.lucifer.pp.common.service.pp.PPFriendService;
import com.lucifer.pp.common.service.pp.PPGroupMemberService;
import com.lucifer.pp.common.service.pp.PPGroupService;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.annotation.CheckLogin;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.ApplyResponseData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.ApplyType;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.util.NetUtil;
import com.lucifer.pp.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ApplyResponseFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.APPLY_RESPONSE;
    private final PPApplicantService applicantService;
    private final SysUserService userService;
    private final PPFriendService friendService;
    private final PPGroupService groupService;
    private final PPGroupMemberService groupMemberService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @Transactional
    @CheckLogin
    public Object apply(Object o) {
        ApplyResponseData data = ((JSONObject) o).toBean(ApplyResponseData.class);
        UpdateWrapper<PPApplicant> applicantUpdate = new UpdateWrapper<>();
        applicantUpdate.eq("applicant_id",data.getApplicantId())
                .eq("receive_id",data.getReceiveId())
                .eq("type",data.getType())
                .isNull("agree")
                .set("agree",data.isAgree()?1:0);
        applicantService.update(applicantUpdate);
        if (data.getType().equals(ApplyType.APPLY_FRIEND.code) && data.isAgree()){      //申请好友同意

            PPFriend ppFriend = new PPFriend();
            if (data.getApplicantId().compareTo(UserContext.getUID()) < 0){
                ppFriend.setUserIdA(data.getApplicantId());
                ppFriend.setUserIdB(UserContext.getUID());
            }else{
                ppFriend.setUserIdA(UserContext.getUID());
                ppFriend.setUserIdB(data.getApplicantId());
            }
            friendService.doAdd(ppFriend);

            SysUser user = userService.getById(data.getApplicantId());
            Friend applicant = Friend.generate(user);
            boolean isOnline = redisUtil.isOnline(user.getId());
            applicant.setOnline(isOnline);
            PPProtocol<Friend> ppProtocol = PPProtocol.of(PPProtocolEnum.ADD_FRIEND,applicant);
                    Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(ppProtocol));
            // 申请者在线
            if (isOnline){
                HeartBeatContext context = redisUtil.getHeartBeatContext(user.getId());
                SysUser response = userService.getById(UserContext.getUID());
                Friend receiver = Friend.generate(response);
                PPProtocol<Friend> ppProtocol2 = PPProtocol.of(PPProtocolEnum.ADD_FRIEND,receiver);
                netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol2));
            }
        }else if (data.getType().equals(ApplyType.APPLY_GROUP.code) && data.isAgree()){     //申请入群，群主/管理员同意

            PPGroupMember ppGroupMember = new PPGroupMember(data.getReceiveId(),data.getApplicantId(),GroupMemberLevel.MEMBER.level);
            groupMemberService.doAdd(ppGroupMember);

            PPGroup group = groupService.getById(data.getReceiveId());
            Group groupDTO = Group.generate(group);
            groupDTO.setMembers(new ArrayList<>());
            List<GroupMember> groupMembers = groupMemberService.queryMemberByGroupId(group.getId());
            GroupMember newMember = GroupMember.generate(userService.getById(data.getApplicantId()));
            newMember.setLevel(GroupMemberLevel.MEMBER.level);
            newMember.setLevelDescription("成员");
            groupDTO.getMembers().add(newMember);

            PPProtocol<Group> addNewMember = PPProtocol.of(PPProtocolEnum.ADD_GROUP_MEMBER,groupDTO);
            //若有群成员在线则通知群成员添加新成员
            groupMembers.forEach(groupMember -> {
                if (redisUtil.isOnline(groupMember.getId()) && !groupMember.getId().equals(data.getApplicantId())){
                    HeartBeatContext context = redisUtil.getHeartBeatContext(groupMember.getId());
                    netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(addNewMember));
                }
            });
            //申请者在线则添加群
            if (redisUtil.isOnline(data.getApplicantId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(data.getApplicantId());
                groupDTO.setMembers(groupMembers);
                PPProtocol<Group> addGroup = PPProtocol.of(PPProtocolEnum.ADD_GROUP,groupDTO);
                netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT,JSONUtil.toJsonStr(addGroup));
            }

        }else if (data.getType().equals(ApplyType.INVITE_GROUP.code) && data.isAgree()){     //邀请入群，被邀请者同意
            PPGroupMember ppGroupMember = new PPGroupMember(data.getApplicantId(),data.getReceiveId(), GroupMemberLevel.MEMBER.level);
            groupMemberService.doAdd(ppGroupMember);

            PPGroup group = groupService.getById(data.getApplicantId());
            Group groupDTO = Group.generate(group);
            groupDTO.setMembers(new ArrayList<>());
            List<GroupMember> groupMembers = groupMemberService.queryMemberByGroupId(group.getId());
            GroupMember newMember = GroupMember.generate(userService.getById(data.getReceiveId()));
            newMember.setLevel(GroupMemberLevel.MEMBER.level);
            newMember.setLevelDescription("成员");
            groupDTO.getMembers().add(newMember);

            PPProtocol<Group> addNewMember = PPProtocol.of(PPProtocolEnum.ADD_GROUP_MEMBER,groupDTO);
            //若有群成员在线则通知群成员添加新成员
            groupMembers.forEach(groupMember -> {
                if (redisUtil.isOnline(groupMember.getId()) && !groupMember.getId().equals(data.getReceiveId())){
                    HeartBeatContext context = redisUtil.getHeartBeatContext(groupMember.getId());
                    netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(addNewMember));
                }
            });
            //被邀请者在线则添加群
            if (redisUtil.isOnline(data.getReceiveId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(data.getReceiveId());
                groupDTO.setMembers(groupMembers);
                PPProtocol<Group> addGroup = PPProtocol.of(PPProtocolEnum.ADD_GROUP,groupDTO);
                netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT,JSONUtil.toJsonStr(addGroup));
            }
        }
        return ChannelContext.release();
    }
}
