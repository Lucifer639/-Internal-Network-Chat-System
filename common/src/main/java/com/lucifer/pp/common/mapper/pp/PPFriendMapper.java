package com.lucifer.pp.common.mapper.pp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.entity.pp.PPFriend;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface PPFriendMapper extends BaseMapper<PPFriend> {
}
