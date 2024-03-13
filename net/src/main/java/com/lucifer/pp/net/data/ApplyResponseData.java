package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyResponseData {
    private String token;
    private Long applicantId;
    private Long receiveId;
    private Integer type;
    private boolean agree;
}
