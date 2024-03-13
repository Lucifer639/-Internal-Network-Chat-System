package com.lucifer.pp.server.function;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.base.ErrorMessage;
import com.lucifer.pp.common.entity.sys.SysUser;
import com.lucifer.pp.common.entity.sys.SysUserPassword;
import com.lucifer.pp.common.service.sys.SysUserPasswordService;
import com.lucifer.pp.common.service.sys.SysUserService;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.RegisterRequestData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import com.lucifer.pp.server.util.MessageGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
public class RegisterRequestFunction implements PPFunction {

    private static final PPProtocolEnum protocol = PPProtocolEnum.REGISTER_REQUEST;

    @Resource
    SysUserService userService;
    @Resource
    SysUserPasswordService userPasswordService;

    @Override
    @Transactional
    public Object apply(Object o) {
        RegisterRequestData data = ((JSONObject) o).toBean(RegisterRequestData.class);
        if (!Validator.isMobile(data.getUserCode())){
            PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.REGISTER_REQUEST, StatusEnum.ERROR, ErrorMessage.USER_CODE_ERROR);
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
            ChannelContext.release();
            return message;
        }
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_code",data.getUserCode());
        List<SysUser> list = userService.list(queryWrapper);
        if (ObjectUtil.isNotEmpty(list)){
            PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.REGISTER_REQUEST, StatusEnum.ERROR, ErrorMessage.USER_CODE_ALREADY_EXIST);
            Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
            ChannelContext.release();
            return message;
        }
        SysUser user = new SysUser(data.getUserCode(),data.getName(), null, System.currentTimeMillis(),1);
        Long uid = userService.doAdd(user);
        SysUserPassword userPassword = new SysUserPassword(uid,data.getPassword(),null,null);
        userPasswordService.doAdd(userPassword);
        PPProtocol<PPMessage> message = MessageGenerator.generate(PPProtocolEnum.REGISTER_REQUEST, StatusEnum.SUCCESS, "注册成功");
        Objects.requireNonNull(ChannelContext.getChannel()).writeAndFlush(JSONUtil.toJsonStr(message));
        return ChannelContext.release();
    }

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }
}
