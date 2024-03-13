package com.lucifer.pp.common.service.pp;

import com.lucifer.pp.common.base.BaseService;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.entity.pp.PPApplicant;

import java.util.List;

public interface PPApplicantService extends BaseService<PPApplicant> {
    List<Application> queryApplication(Long uid);
}
