package com.lucifer.pp.common.service.pp.impl;

import com.lucifer.pp.common.base.BaseServiceImpl;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.entity.pp.PPApplicant;
import com.lucifer.pp.common.mapper.pp.PPApplicantMapper;
import com.lucifer.pp.common.service.pp.PPApplicantService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PPApplicantServiceImpl extends BaseServiceImpl<PPApplicantMapper,PPApplicant> implements PPApplicantService {

    @Resource
    PPApplicantMapper applicantMapper;

    @Override
    public List<Application> queryApplication(Long uid) {
        return applicantMapper.queryApplication(uid);
    }
}
