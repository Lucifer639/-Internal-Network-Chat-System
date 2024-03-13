package com.lucifer.pp.common.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.entity.sys.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser>{
    List<String> getPermissionCode(Long uid);
}
