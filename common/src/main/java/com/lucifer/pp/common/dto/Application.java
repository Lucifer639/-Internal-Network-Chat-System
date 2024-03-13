package com.lucifer.pp.common.dto;

import com.lucifer.pp.common.entity.pp.PPApplicant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    private Long applicantId;
    private Long userId;
    private Long receiveId;
    private String userName;
    private String groupName;
    private String avatar;
    private Integer type;
    private Integer agree;

    public static Application generate(PPApplicant applicant){
        Application app = new Application();
        app.setType(applicant.getType());
        app.setApplicantId(applicant.getApplicantId());
        app.setUserId(applicant.getUserId());
        app.setAgree(applicant.getAgree());
        app.setReceiveId(applicant.getReceiveId());
        return app;
    }

}
