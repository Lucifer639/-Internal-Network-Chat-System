package com.lucifer.pp.server.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.auth.UserContext;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.Application;
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
import com.lucifer.pp.net.data.ApplyRequestData;
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApplyRequestFunction implements PPFunction{

    private final PPApplicantService applicantService;
    private final PPFriendService friendService;
    private final PPGroupService groupService;
    private final PPGroupMemberService groupMemberService;
    private final SysUserService userService;
    private final RedisUtil redisUtil;
    private final NetUtil netUtil;

    private static final PPProtocolEnum protocol = PPProtocolEnum.APPLY_REQUEST;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    @CheckLogin
    @Transactional
    public Object apply(Object o) {
        ApplyRequestData data = ((JSONObject) o).toBean(ApplyRequestData.class);

        PPApplicant applicant = new PPApplicant();

        QueryWrapper<PPApplicant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("applicant_id",data.getApplicantId())
                .eq("receive_id",data.getReceiveId())
                .eq("type",data.getType())
                .isNull("agree");
        PPApplicant one;

        if (data.getType().equals(ApplyType.APPLY_FRIEND.code)){

            //若已为好友关系或已在群中则不处理
            QueryWrapper<PPFriend> friendQuery = new QueryWrapper<>();
            if (Objects.requireNonNull(UserContext.getUID()).compareTo(data.getReceiveId()) > 0){
                friendQuery.eq("user_id_a",data.getReceiveId())
                        .eq("user_id_b",UserContext.getUID());
            }else if (data.getReceiveId().equals(UserContext.getUID())){
                return ChannelContext.release();
            }else{
                friendQuery.eq("user_id_a",UserContext.getUID())
                        .eq("user_id_b",data.getReceiveId());
            }
            PPFriend friendOne = friendService.getOne(friendQuery);
            if (ObjectUtil.isNotEmpty(friendOne)){
                return ChannelContext.release();
            }

            queryWrapper.eq("applicant_id",UserContext.getUID());
            one = applicantService.getOne(queryWrapper);
            applicant.setApplicantId(UserContext.getUID());
        }else{

            QueryWrapper<PPGroupMember> groupMemberQuery = new QueryWrapper<>();
            if (data.getType().equals(ApplyType.APPLY_GROUP.code)){

                groupMemberQuery.eq("group_id",data.getReceiveId())
                        .eq("user_id",UserContext.getUID());
                PPGroupMember groupMemberOne = groupMemberService.getOne(groupMemberQuery);
                if (ObjectUtil.isNotEmpty(groupMemberOne)){
                    return ChannelContext.release();
                }

                queryWrapper.eq("applicant_id",UserContext.getUID());
                one = applicantService.getOne(queryWrapper);
                applicant.setApplicantId(UserContext.getUID());
            }else{

                groupMemberQuery.eq("group_id",data.getApplicantId())
                        .eq("user_id",data.getReceiveId());
                PPGroupMember groupMemberOne = groupMemberService.getOne(groupMemberQuery);
                if (ObjectUtil.isNotEmpty(groupMemberOne)){
                    return ChannelContext.release();
                }

                queryWrapper.eq("applicant_id",data.getApplicantId());
                one = applicantService.getOne(queryWrapper);
                applicant.setApplicantId(data.getApplicantId());
                applicant.setUserId(UserContext.getUID());
            }
        }
        //若存在相同且未处理的申请则不处理
        if (ObjectUtil.isNotEmpty(one)){
            return ChannelContext.release();
        }
        applicant.setReceiveId(data.getReceiveId());
        applicant.setType(data.getType());
        applicantService.doAdd(applicant);
        ChannelContext.release();

        if (data.getType().equals(ApplyType.APPLY_FRIEND.code)){           //查看被申请者是否在线
            if (redisUtil.isOnline(applicant.getReceiveId())){
                HeartBeatContext context = redisUtil.getHeartBeatContext(applicant.getReceiveId());
                Application application = Application.generate(applicant);
                SysUser from = userService.getById(applicant.getApplicantId());
                application.setUserName(from.getName());
                application.setAvatar(from.getAvatar());
                PPProtocol<Application> ppProtocol = PPProtocol.of(PPProtocolEnum.APPLY_REQUEST,application);
                netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
            }
        }else if (data.getType().equals(ApplyType.APPLY_GROUP.code)){      //群主，管理员是否在线
            List<GroupMember> groupMembers = groupMemberService.queryMemberByGroupId(applicant.getReceiveId());
            List<GroupMember> managers = groupMembers.stream().
                    filter(groupMember -> !groupMember.getLevel().equals(GroupMemberLevel.MEMBER.level))
                    .collect(Collectors.toList());
            Application application = Application.generate(applicant);
            SysUser from = userService.getById(applicant.getApplicantId());
            application.setUserName(from.getName());
            application.setAvatar(from.getAvatar());
            PPGroup group = groupService.getById(applicant.getReceiveId());
            application.setGroupName(group.getName());
            PPProtocol<Application> ppProtocol = PPProtocol.of(PPProtocolEnum.APPLY_REQUEST,application);

            managers.forEach(manager -> {
                if (redisUtil.isOnline(manager.getId())){
                    HeartBeatContext context = redisUtil.getHeartBeatContext(manager.getId());
                    netUtil.sendMessage(context.getIp(),BaseConstant.CLIENT_PORT,JSONUtil.toJsonStr(ppProtocol));
                }
            });
        }else if (data.getType().equals(ApplyType.INVITE_GROUP.code) && redisUtil.isOnline(applicant.getReceiveId())){
            HeartBeatContext context = redisUtil.getHeartBeatContext(applicant.getReceiveId());
            Application application = Application.generate(applicant);
            PPGroup group = groupService.getById(applicant.getApplicantId());
            SysUser from = userService.getById(data.getUserId());
            application.setGroupName(group.getName());
            application.setUserName(from.getName());
            application.setAvatar(group.getAvatar());
            PPProtocol<Application> ppProtocol = PPProtocol.of(PPProtocolEnum.APPLY_REQUEST,application);
            netUtil.sendMessage(context.getIp(), BaseConstant.CLIENT_PORT, JSONUtil.toJsonStr(ppProtocol));
        }

        return null;
    }
}
