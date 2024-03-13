package com.lucifer.pp.common.service.pp;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.entity.pp.PPFriend;

import java.util.List;

public interface PPFriendService extends BaseService<PPFriend> {
    List<PPFriend> findFriends(Long uid);
    boolean isFriend(Long userIdA,Long userIdB);
}
