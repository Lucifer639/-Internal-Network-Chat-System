package com.lucifer.pp.common.mapper.pp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.entity.pp.PPApplicant;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PPApplicantMapper extends BaseMapper<PPApplicant> {
    List<Application> queryApplication(Long uid);
}
