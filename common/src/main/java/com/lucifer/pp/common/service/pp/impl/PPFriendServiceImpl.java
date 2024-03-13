package com.lucifer.pp.common.service.pp.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.entity.pp.PPFriend;
import com.lucifer.pp.common.mapper.pp.PPFriendMapper;
import com.lucifer.pp.common.service.pp.PPFriendService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PPFriendServiceImpl extends BaseServiceImpl<PPFriendMapper, PPFriend> implements PPFriendService {

    @Override
    public List<PPFriend> findFriends(Long uid) {
        QueryWrapper<PPFriend> friendQuery = new QueryWrapper<>();
        friendQuery.eq("user_id_a",uid)
                .or()
                .eq("user_id_b",uid);
        return this.list(friendQuery);
    }

    @Override
    public boolean isFriend(Long userIdA, Long userIdB) {
        QueryWrapper<PPFriend> friendQuery = new QueryWrapper<>();
        if (userIdA.compareTo(userIdB) < 0){
            friendQuery.eq("user_id_a",userIdA)
                    .eq("user_id_b",userIdB);
        }else{
            friendQuery.eq("user_id_a",userIdB)
                    .eq("user_id_b",userIdA);
        }
        return ObjectUtil.isNotEmpty(this.getOne(friendQuery));
    }
}
