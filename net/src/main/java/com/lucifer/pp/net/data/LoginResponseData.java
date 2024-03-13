package com.lucifer.pp.net.data;

import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.entity.pp.PPApplicant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseData {
    private String avatar;
    private String userCode;
    private String name;
    private String token;
    private List<Friend> friends;
    private List<Group> groups;
    private List<Application> applications;
}
