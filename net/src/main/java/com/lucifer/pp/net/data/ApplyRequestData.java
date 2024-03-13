package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyRequestData {
    private Integer type;
    private Long applicantId;
    private Long receiveId;
    //当type=2,此字段有效，为邀请人id
    private Long userId;
    private String userName;
    private String groupName;
    private String token;
}
